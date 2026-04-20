const TAB_CONFIG = {
    code: {
        configHeading: '2. Configure Generation',
        generateLabel: 'Generate Process API',
        resultsHeading: '3. Generated Code',
        loadingText: 'Generating code...',
        showLanguage: true,
        showCta: true,
        apiEndpoint: '/api/generate',
        downloadMime: 'text/plain',
    },
    json: {
        configHeading: '2. Select Process Engine',
        generateLabel: 'Generate JSON',
        resultsHeading: '3. Generated JSON',
        loadingText: 'Generating JSON...',
        showLanguage: false,
        showCta: false,
        apiEndpoint: '/api/generate-json',
        downloadMime: 'application/json',
    }
};

// Application State
const state = {
    files: [],
    generatedFiles: [],
    bpmnViewer: null,
    currentBpmnXml: null,
    selectedFileIndex: 0,
    activeTab: 'code'
};

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    loadConfiguration();

    document.getElementById('tab-code').addEventListener('click', () => switchTab('code'));
    document.getElementById('tab-json').addEventListener('click', () => switchTab('json'));

    const params = new URLSearchParams(window.location.search);
    switchTab(params.get('tab') === 'json' ? 'json' : 'code');
});

function switchTab(tab) {
    state.activeTab = tab;
    const cfg = TAB_CONFIG[tab];

    document.querySelectorAll('.tab-btn').forEach(btn =>
        btn.classList.toggle('tab-btn-active', btn.dataset.tab === tab));

    document.getElementById('config-heading').textContent = cfg.configHeading;
    document.getElementById('generate-btn').textContent = cfg.generateLabel;
    document.getElementById('results-heading').textContent = cfg.resultsHeading;
    document.getElementById('loading-text').textContent = cfg.loadingText;

    const languageGroup = document.getElementById('language-group');
    languageGroup.style.display = cfg.showLanguage ? '' : 'none';
    document.getElementById('form-row').classList.toggle('form-row-single', !cfg.showLanguage);

    document.getElementById('results-section').style.display = 'none';
    document.getElementById('cta-section').style.display = 'none';
    document.getElementById('error-message').style.display = 'none';
    document.getElementById('results-content').innerHTML = '';
    state.generatedFiles = [];
}

function setupEventListeners() {
    const fileInput = document.getElementById('file-input');
    const uploadArea = document.getElementById('upload-area');
    const configForm = document.getElementById('config-form');
    const trySampleBtn = document.getElementById('try-sample-btn');

    fileInput.addEventListener('change', handleFileSelect);

    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('drag-over');
    });

    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('drag-over');
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('drag-over');
        const files = Array.from(e.dataTransfer.files).filter(f => f.name.endsWith('.bpmn'));
        if (files.length > 0) {
            addFiles(files);
        }
    });

    configForm.addEventListener('submit', handleGenerate);
    trySampleBtn.addEventListener('click', loadSample);
}

function handleFileSelect(e) {
    const files = Array.from(e.target.files);
    addFiles(files);
    e.target.value = '';
}

function addFiles(newFiles) {
    newFiles.forEach(file => {
        if (!state.files.some(f => f.name === file.name)) {
            state.files.push(file);
        }
    });

    if (state.files.length > 0) {
        document.getElementById('config-section').style.display = 'block';
        selectFile(state.selectedFileIndex);
    }
}

function selectFile(index) {
    if (index < 0 || index >= state.files.length) return;
    state.selectedFileIndex = index;
    renderFileList();

    const file = state.files[index];
    const reader = new FileReader();
    reader.onload = () => {
        state.currentBpmnXml = reader.result;
        renderBpmnDiagram(reader.result);
    };
    reader.readAsText(file);
}

function removeFile(fileName) {
    state.files = state.files.filter(f => f.name !== fileName);

    if (state.files.length === 0) {
        document.getElementById('config-section').style.display = 'none';
        document.getElementById('results-section').style.display = 'none';
        document.getElementById('bpmn-viewer-container').style.display = 'none';
        document.getElementById('cta-section').style.display = 'none';
        state.currentBpmnXml = null;
        state.selectedFileIndex = 0;
        renderFileList();
    } else {
        if (state.selectedFileIndex >= state.files.length) {
            state.selectedFileIndex = state.files.length - 1;
        }
        selectFile(state.selectedFileIndex);
    }
}

function renderFileList() {
    const fileList = document.getElementById('file-list');
    if (state.files.length === 0) {
        fileList.innerHTML = '';
        return;
    }

    fileList.innerHTML = state.files.map((file, index) => `
        <div class="file-item ${index === state.selectedFileIndex ? 'file-item-active' : ''}" onclick="selectFile(${index})">
            <div class="file-item-info">
                <span class="file-item-name">${escapeHtml(file.name)}</span>
                <span class="file-item-size">${formatFileSize(file.size)}</span>
            </div>
            <button type="button" class="file-item-remove" onclick="event.stopPropagation(); removeFile('${escapeHtml(file.name)}')">&times;</button>
        </div>
    `).join('');
}

async function renderBpmnDiagram(xml) {
    const bpmnViewerContainer = document.getElementById('bpmn-viewer-container');
    if (state.bpmnViewer) {
        state.bpmnViewer.destroy();
        state.bpmnViewer = null;
    }

    bpmnViewerContainer.style.display = 'block';
    await new Promise(resolve => setTimeout(resolve, 0));

    state.bpmnViewer = new BpmnJS({ container: '#bpmn-canvas' });
    try {
        await state.bpmnViewer.importXML(xml);
    } catch (err) {
        console.error('Failed to import BPMN diagram:', err);
        bpmnViewerContainer.style.display = 'none';
        return;
    }

    const canvas = state.bpmnViewer.get('canvas');
    for (let attempt = 0; attempt < 5; attempt++) {
        try {
            canvas.zoom('fit-viewport');
            const currentZoom = canvas.zoom();
            canvas.zoom(currentZoom * 0.92);
            return;
        } catch (err) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }
    }
    console.warn('Could not fit viewport, diagram rendered at default zoom');
}

async function loadSample() {
    const trySampleBtn = document.getElementById('try-sample-btn');
    try {
        trySampleBtn.disabled = true;
        trySampleBtn.textContent = 'Loading...';

        const response = await fetch('/samples/c8-newsletter.bpmn');
        if (!response.ok) throw new Error('Failed to fetch sample');
        const xml = await response.text();

        const blob = new Blob([xml], { type: 'application/xml' });
        const file = new File([blob], 'c8-newsletter.bpmn', { type: 'application/xml' });

        state.files = [];
        addFiles([file]);

        document.getElementById('process-engine').value = 'ZEEBE';

    } catch (err) {
        showError('Failed to load sample: ' + err.message);
    } finally {
        trySampleBtn.disabled = false;
        trySampleBtn.textContent = 'Try with Newsletter Sample';
    }
}

async function handleGenerate(e) {
    e.preventDefault();
    const cfg = TAB_CONFIG[state.activeTab];

    const resultsSection = document.getElementById('results-section');
    const errorMessage = document.getElementById('error-message');
    const loading = document.getElementById('loading');
    const generateBtn = document.getElementById('generate-btn');

    resultsSection.style.display = 'none';
    errorMessage.style.display = 'none';
    document.getElementById('cta-section').style.display = 'none';
    loading.style.display = 'block';
    generateBtn.disabled = true;

    try {
        const filesData = await Promise.all(
            state.files.map(async (file) => ({
                fileName: file.name,
                content: await readFileAsBase64(file)
            }))
        );

        const config = { processEngine: document.getElementById('process-engine').value };
        if (cfg.showLanguage) {
            config.outputLanguage = document.getElementById('output-language').value;
        }

        const response = await fetch(cfg.apiEndpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ files: filesData, config })
        });

        const result = await response.json();

        if (result.success) {
            state.generatedFiles = result.files;
            renderResults(result.files);
            resultsSection.style.display = 'block';
            if (cfg.showCta) {
                document.getElementById('cta-section').style.display = 'block';
            }
            resultsSection.scrollIntoView({ behavior: 'smooth' });
        } else {
            showError(result.error || 'Unknown error occurred');
        }

    } catch (error) {
        console.error('Generation error:', error);
        showError(`Failed to generate: ${error.message}`);
    } finally {
        loading.style.display = 'none';
        generateBtn.disabled = false;
    }
}

function renderResults(files) {
    const languageClass = state.activeTab === 'json'
        ? 'json'
        : (document.getElementById('output-language').value === 'JAVA' ? 'java' : 'kotlin');

    document.getElementById('results-content').innerHTML = files.map((file, index) => `
        <div class="code-file">
            <div class="code-file-header">
                <div>
                    <div class="code-file-title">${escapeHtml(file.fileName)}</div>
                    <div class="code-file-meta">Process: ${escapeHtml(file.processId)}</div>
                </div>
                <div class="code-file-actions">
                    <button class="btn-copy" onclick="copyToClipboard(${index})">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                        </svg>
                        Copy
                    </button>
                    <button class="btn-download" onclick="downloadFile('${escapeHtml(file.fileName)}', ${index})">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                            <polyline points="7 10 12 15 17 10"></polyline>
                            <line x1="12" y1="15" x2="12" y2="3"></line>
                        </svg>
                        Download
                    </button>
                </div>
            </div>
            <div class="code-preview">
                <pre><code class="language-${languageClass}">${escapeHtml(file.content)}</code></pre>
            </div>
        </div>
    `).join('');

    document.querySelectorAll('pre code').forEach((block) => {
        hljs.highlightElement(block);
    });
}

function copyToClipboard(index) {
    const file = state.generatedFiles[index];

    navigator.clipboard.writeText(file.content).then(() => {
        const buttons = document.querySelectorAll('.btn-copy');
        const button = buttons[index];
        const originalHTML = button.innerHTML;

        button.innerHTML = `
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
            Copied!
        `;
        button.style.background = '#10b981';

        setTimeout(() => {
            button.innerHTML = originalHTML;
            button.style.background = '';
        }, 2000);
    }).catch(err => {
        console.error('Failed to copy:', err);
        showError('Failed to copy to clipboard');
    });
}

function downloadFile(fileName, index) {
    const file = state.generatedFiles[index];
    const blob = new Blob([file.content], { type: TAB_CONFIG[state.activeTab].downloadMime });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function showError(message) {
    const errorMessage = document.getElementById('error-message');
    errorMessage.innerHTML = escapeHtml(message).replace(/\n/g, '<br>');
    errorMessage.style.display = 'block';
    errorMessage.scrollIntoView({ behavior: 'smooth' });
}

function readFileAsBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
            const base64 = reader.result.split(',')[1];
            resolve(base64);
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

async function loadConfiguration() {
    try {
        const response = await fetch('/api/config');
        const config = await response.json();

        if (config.version) {
            const badge = document.getElementById('version-badge');
            badge.textContent = config.version;
            badge.style.display = 'inline-flex';
        }

        if (config.legalLinks) {
            setupLegalLinks(config.legalLinks);
        }
    } catch (error) {
        console.error('Failed to load configuration:', error);
    }
}

function setupLegalLinks(legalLinks) {
    const imprintLink = document.getElementById('imprint-link');
    const privacyLink = document.getElementById('privacy-link');
    const separator = document.getElementById('legal-separator');

    if (legalLinks.imprintUrl) {
        imprintLink.href = legalLinks.imprintUrl;
        imprintLink.style.display = 'inline';
    } else {
        imprintLink.style.display = 'none';
    }

    if (legalLinks.privacyUrl) {
        privacyLink.href = legalLinks.privacyUrl;
        privacyLink.style.display = 'inline';
    } else {
        privacyLink.style.display = 'none';
    }

    if (legalLinks.imprintUrl && legalLinks.privacyUrl) {
        separator.style.display = 'inline';
    } else {
        separator.style.display = 'none';
    }
}
