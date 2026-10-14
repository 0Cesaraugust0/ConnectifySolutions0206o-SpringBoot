(function () {
    let activeRequest;

    function syncLinks(page) {
        const currentLinks = document.querySelectorAll('a[data-communications-filter]');
        const nextLinks = page.querySelectorAll('a[data-communications-filter]');

        currentLinks.forEach(function (link, index) {
            const nextLink = nextLinks[index];
            if (!nextLink) return;
            link.href = nextLink.href;
            link.className = nextLink.className;
            if (nextLink.hasAttribute('aria-current')) {
                link.setAttribute('aria-current', nextLink.getAttribute('aria-current'));
            } else {
                link.removeAttribute('aria-current');
            }
        });
    }

    function applyPage(page) {
        const currentHero = document.getElementById('communicationsHero');
        const nextHero = page.getElementById('communicationsHero');
        const currentResults = document.getElementById('communicationsResults');
        const nextResults = page.getElementById('communicationsResults');

        if (!currentHero || !nextHero || !currentResults || !nextResults) {
            throw new Error('La respuesta no contiene el buzón completo.');
        }

        currentHero.className = nextHero.className;
        currentHero.innerHTML = nextHero.innerHTML;
        currentResults.innerHTML = nextResults.innerHTML;
        syncLinks(page);
    }

    function updateView(page) {
        if (document.startViewTransition && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            document.startViewTransition(function () {
                applyPage(page);
            });
            return;
        }
        applyPage(page);
    }

    async function refreshInbox(url, updateHistory) {
        const pageRoot = document.querySelector('.communications-page');
        const results = document.getElementById('communicationsResults');
        if (!pageRoot || !results) return;

        if (activeRequest) activeRequest.abort();
        const request = new AbortController();
        activeRequest = request;
        pageRoot.classList.add('is-filtering');
        results.setAttribute('aria-busy', 'true');

        try {
            const response = await fetch(url, {
                signal: request.signal,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            if (!response.ok) throw new Error('No se pudo actualizar el buzón.');

            const page = new DOMParser().parseFromString(await response.text(), 'text/html');
            updateView(page);
            if (updateHistory) history.pushState({ communicationsFilter: true }, '', url);
        } catch (error) {
            if (error.name === 'AbortError') return;
            window.location.assign(url);
        } finally {
            if (request === activeRequest) {
                pageRoot.classList.remove('is-filtering');
                results.removeAttribute('aria-busy');
            }
        }
    }

    document.addEventListener('click', function (event) {
        const link = event.target.closest('a[data-communications-filter]');
        if (!link || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;

        event.preventDefault();
        link.parentElement.querySelectorAll('a[data-communications-filter]').forEach(function (item) {
            const selected = item === link;
            item.classList.toggle('active', selected);
            if (selected) item.setAttribute('aria-current', 'page');
            else item.removeAttribute('aria-current');
        });
        refreshInbox(link.href, true);
    });

    window.addEventListener('popstate', function () {
        refreshInbox(window.location.href, false);
    });
})();
