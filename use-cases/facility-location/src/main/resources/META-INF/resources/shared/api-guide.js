// ── API usage guide ──
// A self-contained <api-guide-modal> custom element: a full-screen overlay documenting this
// quickstart's REST API via copy-pasteable cURL commands (see solver-client.js for the
// endpoints it documents).
customElements.define('api-guide-modal', class extends HTMLElement {
    connectedCallback() {
        document.getElementById('apiGuideButton')?.addEventListener('click', () => this.open());
    }

    open() {
        if (!this.modalEl) {
            this.render();
        }
        bootstrap.Modal.getOrCreateInstance(this.modalEl).show();
    }

    render() {
        const modelPath = this.getAttribute('model-path');
        const demoDataId = this.getAttribute('demo-data-id');
        if (!modelPath) {
            throw new Error('<api-guide-modal> requires a model-path attribute, e.g. "/v1/schedules".');
        }
        if (!demoDataId) {
            throw new Error('<api-guide-modal> requires a demo-data-id attribute, e.g. "BASIC".');
        }
        const demoDataPath = '/v1/demo-data';
        const steps = [
            {
                title: '1. List the available demo datasets',
                curl: `curl -X GET -H 'Accept:application/json' http://localhost:8080${demoDataPath}`,
            },
            {
                title: '2. Download a demo dataset',
                curl: `curl -X GET -H 'Accept:application/json' http://localhost:8080${demoDataPath}/${demoDataId} -o sample.json`,
            },
            {
                title: '3. Submit it for solving',
                description: 'The POST operation returns an <code>id</code> - the jobId used in every command below.',
                curl: `curl -X POST -H 'Content-Type:application/json' http://localhost:8080${modelPath} -d @sample.json`,
            },
            {
                title: '4. Get the current status and result',
                curl: `curl -X GET -H 'Accept:application/json' http://localhost:8080${modelPath}/{jobId}`,
            },
            {
                title: '5. Get the score analysis',
                curl: `curl -X GET -H 'Accept:application/json' "http://localhost:8080${modelPath}/{jobId}/score-analysis?includeJustifications=true"`,
            },
            {
                title: '6. Terminate solving early',
                curl: `curl -X DELETE -H 'Accept:application/json' http://localhost:8080${modelPath}/{jobId}`,
            },
        ];

        this.innerHTML = `
    <div class="modal fade" id="apiGuideModal" tabindex="-1" aria-labelledby="apiGuideModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-fullscreen">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="apiGuideModalLabel">REST API Guide</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <h2>Integration via cURL</h2>
                    ${steps.map((step, index) => `
                    <h3>${step.title}</h3>
                    ${step.description ? `<p>${step.description}</p>` : ''}
                    <pre><button type="button" class="btn btn-outline-dark btn-sm float-end" data-copy-index="${index}">Copy</button><code id="apiGuideCurl${index}">${escapeHtml(step.curl)}</code></pre>`).join('')}
                </div>
            </div>
        </div>
    </div>`;

        this.querySelectorAll('button[data-copy-index]').forEach((button) => {
            button.addEventListener('click', () => {
                const code = this.querySelector(`#apiGuideCurl${button.dataset.copyIndex}`).textContent;
                navigator.clipboard.writeText(code);
            });
        });

        this.modalEl = this.querySelector('#apiGuideModal');
    }
});

function escapeHtml(value) {
    return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
