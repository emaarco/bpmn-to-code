# Deployment Plan: bpmn-to-code Web Application

**Issue:** [#66](https://github.com/emaarco/bpmn-to-code/issues/66)
**Status:** MVP Complete, ready for deployment
**Goal:** Deploy web application with free/low-cost hosting

---

## Overview

The web application has two components:
1. **Frontend** (static HTML/CSS/JS) - can be hosted on GitHub Pages
2. **Backend** (Ktor JVM app) - needs a server, cannot run on GitHub Pages

## Deployment Strategy

### Option 1: Hybrid Deployment (Recommended)
**Frontend:** GitHub Pages (free)
**Backend:** Render.com free tier (or Railway/Fly.io)
**Cost:** $0/month

**Pros:**
- Completely free
- Frontend on GitHub = fast CDN delivery
- Easy to update (push to branch → auto-deploy)
- Professional setup

**Cons:**
- Need to configure CORS
- Backend on free tier may cold-start (slower first request)

### Option 2: All-in-One Deployment
**Both Frontend + Backend:** Single Render/Railway/Fly.io instance
**Cost:** $0/month (free tier)

**Pros:**
- Simpler setup (one deployment)
- No CORS configuration needed
- Single URL

**Cons:**
- No GitHub Pages CDN benefits
- Slightly more resource usage

---

## Recommended: Option 1 (Hybrid Deployment)

This gives you the best of both worlds - fast frontend delivery via GitHub Pages CDN and a free backend tier.

---

## Step-by-Step Implementation

### Phase 1: Deploy Backend to Render.com

#### 1.1 Prepare the Backend

**Create `render.yaml` in project root:**

```yaml
services:
  - type: web
    name: bpmn-to-code-api
    env: docker
    dockerfilePath: ./bpmn-to-code-web/Dockerfile
    dockerContext: .
    plan: free
    healthCheckPath: /health
    envVars:
      - key: PORT
        value: 8080
      - key: ALLOWED_ORIGINS
        value: https://<your-username>.github.io
```

**Update Application.kt to support dynamic CORS:**

```kotlin
// In configureApp()
install(CORS) {
    val allowedOrigins = System.getenv("ALLOWED_ORIGINS")?.split(",")
        ?: listOf("http://localhost:3000")

    allowedOrigins.forEach { origin ->
        allowHost(origin.removePrefix("https://").removePrefix("http://"))
    }

    allowHeader(HttpHeaders.ContentType)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Options)
}
```

**Update Application.kt to use PORT env variable:**

```kotlin
fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        configureApp()
    }.start(wait = true)
}
```

#### 1.2 Deploy to Render

**Option A: Via Render Dashboard**
1. Go to [render.com](https://render.com)
2. Sign up with GitHub
3. Click "New +" → "Web Service"
4. Connect your `bpmn-to-code` repository
5. Select branch: `66-web-application`
6. Settings:
   - **Name:** `bpmn-to-code-api`
   - **Environment:** `Docker`
   - **Dockerfile Path:** `./bpmn-to-code-web/Dockerfile`
   - **Docker Context:** `.` (project root)
   - **Plan:** Free
7. Environment Variables:
   - `PORT`: `8080`
   - `ALLOWED_ORIGINS`: `https://<your-username>.github.io` (update after GitHub Pages setup)
8. Click "Create Web Service"

**Option B: Via Render Blueprint (render.yaml)**
1. Push `render.yaml` to your branch
2. Go to Render Dashboard → "New +" → "Blueprint"
3. Select your repository and branch
4. Render will auto-detect `render.yaml` and deploy

**Expected Result:**
- Backend URL: `https://bpmn-to-code-api.onrender.com`
- Health check: `https://bpmn-to-code-api.onrender.com/health` returns `{"status":"UP"}`

**Note:** Free tier cold starts after 15 min inactivity (first request takes ~30s)

---

### Phase 2: Deploy Frontend to GitHub Pages

#### 2.1 Prepare Frontend for GitHub Pages

**Create `bpmn-to-code-web/static-dist/` directory:**

Copy static files and update API URL to point to production backend.

**Create build script: `bpmn-to-code-web/build-frontend.sh`:**

```bash
#!/bin/bash
set -e

BACKEND_URL=${1:-"https://bpmn-to-code-api.onrender.com"}

echo "Building frontend for production..."
echo "Backend URL: $BACKEND_URL"

# Create dist directory
rm -rf static-dist
mkdir -p static-dist/css static-dist/js

# Copy static files
cp src/main/resources/static/index.html static-dist/
cp src/main/resources/static/css/styles.css static-dist/css/
cp src/main/resources/static/js/app.js static-dist/js/

# Update API URL in JavaScript
sed -i.bak "s|'/api/generate'|'${BACKEND_URL}/api/generate'|g" static-dist/js/app.js
rm static-dist/js/app.js.bak

echo "Frontend built successfully in static-dist/"
```

**Make executable:**
```bash
chmod +x bpmn-to-code-web/build-frontend.sh
```

#### 2.2 Configure GitHub Pages

**Option A: Manual Deploy**

1. Build frontend:
```bash
cd bpmn-to-code-web
./build-frontend.sh https://bpmn-to-code-api.onrender.com
```

2. Create `gh-pages` branch:
```bash
cd static-dist
git init
git checkout -b gh-pages
git add .
git commit -m "Deploy frontend to GitHub Pages"
git remote add origin git@github.com:<your-username>/bpmn-to-code.git
git push -f origin gh-pages
```

3. Enable GitHub Pages:
   - Go to repository Settings → Pages
   - Source: `gh-pages` branch
   - Directory: `/` (root)
   - Save

**Option B: Automated with GitHub Actions**

**Create `.github/workflows/deploy-web-app.yml`:**

```yaml
name: Deploy Web Application

on:
  push:
    branches:
      - 66-web-application
    paths:
      - 'bpmn-to-code-web/**'
  workflow_dispatch:

jobs:
  deploy-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Build frontend
        working-directory: bpmn-to-code-web
        run: |
          chmod +x build-frontend.sh
          ./build-frontend.sh ${{ secrets.BACKEND_URL }}

      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./bpmn-to-code-web/static-dist
          publish_branch: gh-pages
          cname: bpmn-to-code.io  # Optional: custom domain
```

**Add secret to GitHub:**
- Go to Settings → Secrets → Actions
- Add: `BACKEND_URL` = `https://bpmn-to-code-api.onrender.com`

**Expected Result:**
- Frontend URL: `https://<your-username>.github.io/bpmn-to-code/`

---

### Phase 3: Connect Frontend ↔ Backend

#### 3.1 Update CORS on Backend

Update Render environment variable:
- `ALLOWED_ORIGINS` = `https://<your-username>.github.io`

Or if using custom domain:
- `ALLOWED_ORIGINS` = `https://bpmn-to-code.io`

Render will auto-restart with new config.

#### 3.2 Test Integration

1. Visit: `https://<your-username>.github.io/bpmn-to-code/`
2. Upload a BPMN file
3. Click "Generate"
4. Verify code generation works

**Troubleshooting:**
- Check browser console for CORS errors
- Verify backend health: `https://bpmn-to-code-api.onrender.com/health`
- Check Render logs for backend errors

---

## Alternative: All-in-One Deployment (Simpler)

If you prefer simplicity over GitHub Pages CDN:

### Deploy Everything to Render

Use the existing Ktor app (no changes needed):

**render.yaml:**
```yaml
services:
  - type: web
    name: bpmn-to-code-web
    env: docker
    dockerfilePath: ./bpmn-to-code-web/Dockerfile
    dockerContext: .
    plan: free
    healthCheckPath: /health
```

**Deploy:**
1. Push code to branch
2. Connect to Render (as in Phase 1)
3. Access: `https://bpmn-to-code-web.onrender.com`

Done! No CORS, no GitHub Pages setup needed.

---

## Custom Domain (Optional)

### For GitHub Pages Frontend

1. Buy domain (e.g., `bpmn-to-code.io`)
2. Add CNAME record: `bpmn-to-code.io` → `<your-username>.github.io`
3. Add `CNAME` file to `static-dist/`:
   ```
   bpmn-to-code.io
   ```
4. In GitHub Settings → Pages, set custom domain
5. Enable HTTPS

### For Render Backend

1. In Render dashboard → Settings → Custom Domain
2. Add: `api.bpmn-to-code.io`
3. Follow DNS instructions (add CNAME)
4. Render auto-provisions SSL

**Final URLs:**
- Frontend: `https://bpmn-to-code.io`
- Backend: `https://api.bpmn-to-code.io`

---

## Deployment Checklist

### Pre-Deployment
- [ ] Update `Application.kt` to read `PORT` env variable
- [ ] Update `Application.kt` for dynamic CORS origins
- [ ] Create `render.yaml` configuration
- [ ] Create frontend build script
- [ ] Test locally with production-like config

### Backend Deployment (Render)
- [ ] Create Render account
- [ ] Connect GitHub repository
- [ ] Deploy from `66-web-application` branch
- [ ] Set environment variables (`PORT`, `ALLOWED_ORIGINS`)
- [ ] Verify health endpoint works
- [ ] Test `/api/generate` with curl/Postman

### Frontend Deployment (GitHub Pages)
- [ ] Build frontend with production backend URL
- [ ] Create `gh-pages` branch (or use GitHub Actions)
- [ ] Enable GitHub Pages in repository settings
- [ ] Verify static site loads
- [ ] Update CORS on backend with GitHub Pages URL

### Integration Testing
- [ ] Test full flow: upload → generate → download
- [ ] Check browser console for errors
- [ ] Test on mobile devices
- [ ] Verify syntax highlighting works
- [ ] Test error handling (invalid files, network errors)

### Post-Deployment
- [ ] Update issue #66 with deployment URLs
- [ ] Update main README with web app link
- [ ] Announce on social media / forums
- [ ] Monitor Render logs for errors
- [ ] Gather user feedback

---

## Maintenance & Monitoring

### Render Free Tier Limits
- **Cold starts:** Service sleeps after 15min inactivity
- **Build time:** Max 10 minutes (current build: ~2 min)
- **Bandwidth:** 100GB/month
- **Build hours:** 500 hours/month

**Mitigation for cold starts:**
- Add health check pings (cron-job.org or UptimeRobot)
- Warn users on UI: "First request may take 30s"
- Consider paid plan ($7/mo) if traffic grows

### Monitoring
- **Render Dashboard:** View logs, metrics, deploy history
- **GitHub Actions:** Monitor deployment status
- **Health check:** Set up UptimeRobot for `/health` endpoint
- **Analytics:** Add simple analytics (Plausible or Google Analytics)

---

## Cost Analysis

### Recommended Setup (Hybrid)
| Component | Service | Cost |
|-----------|---------|------|
| Frontend | GitHub Pages | Free |
| Backend | Render Free Tier | Free |
| **Total** | | **$0/month** |

### With Custom Domain
| Component | Service | Cost |
|-----------|---------|------|
| Frontend | GitHub Pages | Free |
| Backend | Render Free Tier | Free |
| Domain | Namecheap/Cloudflare | ~$12/year |
| **Total** | | **~$1/month** |

### Upgrade Path (if needed)
| Component | Service | Cost |
|-----------|---------|------|
| Frontend | GitHub Pages | Free |
| Backend | Render Starter | $7/month |
| Domain | Your registrar | ~$12/year |
| **Total** | | **~$8/month** |

---

## Rollout Plan

### Week 1: Initial Deployment
- [ ] Deploy backend to Render (free tier)
- [ ] Deploy frontend to GitHub Pages
- [ ] Test integration thoroughly
- [ ] Fix any deployment issues

### Week 2: Soft Launch
- [ ] Share with small group (Discord, Slack)
- [ ] Gather initial feedback
- [ ] Monitor logs for errors
- [ ] Make quick fixes if needed

### Week 3: Public Announcement
- [ ] Update main README
- [ ] Create blog post / announcement
- [ ] Share on social media
- [ ] Post on relevant forums (Reddit, HN)

### Week 4: Iterate
- [ ] Analyze usage metrics
- [ ] Implement quick wins from feedback
- [ ] Consider custom domain
- [ ] Plan next features

---

## Next Immediate Steps

1. **Update Application.kt** (dynamic PORT and CORS)
2. **Create render.yaml** in project root
3. **Create build-frontend.sh** script
4. **Deploy backend to Render** (10 minutes)
5. **Test backend API** with curl
6. **Build and deploy frontend** to GitHub Pages (10 minutes)
7. **Test complete flow** end-to-end

**Time estimate:** 1-2 hours for complete deployment

---

## Alternative Hosting Options

If you prefer not to use Render:

### Railway.app
- Similar to Render
- Free tier: $5 credit/month (enough for small project)
- Deploy from Dockerfile
- Better DX, slightly more generous free tier

### Fly.io
- Free tier: 3 VMs with 256MB RAM
- Deploy with `fly launch`
- Global edge deployment
- More complex setup but powerful

### Vercel (Backend Limitations)
- Great for frontend (free)
- Backend: Serverless functions only (not suitable for long-running JVM app)
- Not recommended for this use case

---

## Summary

**Recommended path:**
1. ✅ Backend on Render (free, Docker-based, easy setup)
2. ✅ Frontend on GitHub Pages (free, CDN, auto-deploy)
3. ✅ Start with free tiers, upgrade if traffic grows

**Simplest path:**
1. ✅ Everything on Render (single deployment)
2. ✅ One URL, no CORS complexity

Both paths are free and production-ready. Choose based on your preference for simplicity vs. optimization.
