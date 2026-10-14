document.addEventListener("DOMContentLoaded", () => {
    const filters = [...document.querySelectorAll("[data-ticket-filter]")];
    const tickets = [...document.querySelectorAll("[data-ticket-status]")];
    const emptyState = document.querySelector(".client-ticket-filter-empty");

    if (!filters.length || !tickets.length) {
        return;
    }

    filters.forEach((button) => {
        button.addEventListener("click", () => {
            const selectedStatus = button.dataset.ticketFilter;
            let visibleTickets = 0;

            filters.forEach((filter) => {
                const active = filter === button;
                filter.classList.toggle("active", active);
                filter.setAttribute("aria-pressed", String(active));
            });

            tickets.forEach((ticket) => {
                const visible = selectedStatus === "all" || ticket.dataset.ticketStatus === selectedStatus;
                ticket.hidden = !visible;
                if (visible) visibleTickets += 1;
            });

            if (emptyState) {
                emptyState.hidden = visibleTickets !== 0;
            }
        });
    });
});
