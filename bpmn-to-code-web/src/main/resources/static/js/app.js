// Application State
const state = {
    files: [],
    generatedFiles: []
};

// DOM Elements
const fileInput = document.getElementById('file-input');
const uploadArea = document.getElementById('upload-area');
const fileList = document.getElementById('file-list');
const configSection = document.getElementById('config-section');
const resultsSection = document.getElementById('results-section');
const configForm = document.getElementById('config-form');
const generateBtn = document.getElementById('generate-btn');
const loading = document.getElementById('loading');
const errorMessage = document.getElementById('error-message');
const resultsContent = document.getElementById('results-content');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    loadConfiguration();
});

function setupEventListeners() {
    // File input
    fileInput.addEventListener('change', handleFileSelect);

    // Drag and drop
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

    // Form submission
    configForm.addEventListener('submit', handleGenerate);
}

function handleFileSelect(e) {
    const files = Array.from(e.target.files);
    addFiles(files);
    e.target.value = ''; // Reset input to allow re-selecting same file
}

function addFiles(newFiles) {
    // Add new files to state
    newFiles.forEach(file => {
        if (!state.files.some(f => f.name === file.name)) {
            state.files.push(file);
        }
    });

    renderFileList();

    // Show config section if files are uploaded
    if (state.files.length > 0) {
        configSection.style.display = 'block';
    }
}

function removeFile(fileName) {
    state.files = state.files.filter(f => f.name !== fileName);
    renderFileList();

    // Hide config if no files
    if (state.files.length === 0) {
        configSection.style.display = 'none';
        resultsSection.style.display = 'none';
    }
}

function renderFileList() {
    if (state.files.length === 0) {
        fileList.innerHTML = '';
        return;
    }

    fileList.innerHTML = state.files.map(file => `
        <div class="file-item">
            <div>
                <span class="file-item-name">${escapeHtml(file.name)}</span>
                <span class="file-item-size">(${formatFileSize(file.size)})</span>
            </div>
            <button type="button" class="file-item-remove" onclick="removeFile('${escapeHtml(file.name)}')">&times;</button>
        </div>
    `).join('');
}

async function handleGenerate(e) {
    e.preventDefault();

    // Hide previous results and errors
    resultsSection.style.display = 'none';
    errorMessage.style.display = 'none';
    loading.style.display = 'block';
    generateBtn.disabled = true;

    try {
        // Read and encode files
        const filesData = await Promise.all(
            state.files.map(async (file) => ({
                fileName: file.name,
                content: await readFileAsBase64(file)
            }))
        );

        // Get config
        const config = {
            outputLanguage: document.getElementById('output-language').value,
            processEngine: document.getElementById('process-engine').value
        };

        // Call API
        const response = await fetch('/api/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                files: filesData,
                config: config
            })
        });

        const result = await response.json();

        if (result.success) {
            state.generatedFiles = result.files;
            renderResults(result.files, config.outputLanguage);
            resultsSection.style.display = 'block';

            // Scroll to results
            resultsSection.scrollIntoView({behavior: 'smooth'});
        } else {
            showError(result.error || 'Unknown error occurred');
        }

    } catch (error) {
        console.error('Generation error:', error);
        showError(`Failed to generate code: ${error.message}`);
    } finally {
        loading.style.display = 'none';
        generateBtn.disabled = false;
    }
}

function renderResults(files, language) {
    const languageClass = language === 'JAVA' ? 'java' : 'kotlin';

    resultsContent.innerHTML = files.map(file => `
        <div class="code-file">
            <div class="code-file-header">
                <div>
                    <div class="code-file-title">${escapeHtml(file.fileName)}</div>
                    <div class="code-file-meta">Process: ${escapeHtml(file.processId)}</div>
                </div>
                <div class="code-file-actions">
                    <button class="btn-copy" onclick="copyToClipboard(${files.indexOf(file)})">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                        </svg>
                        Copy
                    </button>
                    <button class="btn-download" onclick="downloadFile('${escapeHtml(file.fileName)}', ${files.indexOf(file)})">
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

    // Apply syntax highlighting
    document.querySelectorAll('pre code').forEach((block) => {
        hljs.highlightElement(block);
    });
}

function copyToClipboard(index) {
    const file = state.generatedFiles[index];

    navigator.clipboard.writeText(file.content).then(() => {
        // Visual feedback - change button text temporarily
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
    const blob = new Blob([file.content], {type: 'text/plain'});
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
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
    errorMessage.scrollIntoView({behavior: 'smooth'});
}

// Utility Functions
function readFileAsBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
            // Remove data URL prefix (data:application/octet-stream;base64,)
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

// Load configuration from backend
async function loadConfiguration() {
    try {
        const response = await fetch('/api/config');
        const config = await response.json();

        if (config.legalLinks) {
            setupLegalLinks(config.legalLinks);
        }
    } catch (error) {
        console.error('Failed to load configuration:', error);
        // Silently fail - legal links are optional
    }
}

function setupLegalLinks(legalLinks) {
    const imprintLink = document.getElementById('imprint-link');
    const privacyLink = document.getElementById('privacy-link');
    const separator = document.getElementById('legal-separator');

    // Show/hide links based on whether URLs are provided
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

    // Show separator only if both links are configured
    if (legalLinks.imprintUrl && legalLinks.privacyUrl) {
        separator.style.display = 'inline';
    } else {
        separator.style.display = 'none';
    }
}
