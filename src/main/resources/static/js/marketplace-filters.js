(function () {
    let activeRequest;

    async function refreshResults(url, updateHistory) {
        const currentResults = document.getElementById('marketplaceResults');
        if (!currentResults) return;

        if (activeRequest) activeRequest.abort();
        activeRequest = new AbortController();
        currentResults.classList.add('is-loading');
        currentResults.setAttribute('aria-busy', 'true');

        try {
            const response = await fetch(url, {
                signal: activeRequest.signal,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            if (!response.ok) throw new Error('No se pudieron actualizar los eventos.');

            const page = new DOMParser().parseFromString(await response.text(), 'text/html');
            const nextResults = page.getElementById('marketplaceResults');
            if (!nextResults) throw new Error('La respuesta no contiene resultados.');

            currentResults.replaceWith(nextResults);
            if (updateHistory) history.pushState({ marketplaceFilter: true }, '', url);
        } catch (error) {
            if (error.name === 'AbortError') return;
            window.location.assign(url);
        } finally {
            const visibleResults = document.getElementById('marketplaceResults');
            if (visibleResults) {
                visibleResults.classList.remove('is-loading');
                visibleResults.removeAttribute('aria-busy');
            }
        }
    }

    document.addEventListener('click', function (event) {
        const link = event.target.closest('a[data-marketplace-filter]');
        if (!link || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
        event.preventDefault();
        refreshResults(link.href, true);
    });

    window.addEventListener('popstate', function () {
        refreshResults(window.location.href, false);
    });
})();
