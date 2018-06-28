function urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding)
        .replace(/\-/g, '+')
        .replace(/_/g, '/');

    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);

    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
}

function getNotificationPermissionState() {
    if (navigator.permissions) {
        return navigator.permissions.query({name: 'notifications'})
            .then((result) => {
                return result.state;
            });
    }

    return new Promise((resolve) => {
        resolve(Notification.permission);
    });
}


function toggleNotificationControls() {
    getNotificationPermissionState()
        .then((state) => {
            console.log('Permission state: ', state)
            var notificationsBlocked = $("div[id='notifications-blocked-div']")
            var subscribeButton = $("div[id='subscribe-button-div']")
            if (state == 'denied') {
                notificationsBlocked.removeClass('d-none')
                if (!subscribeButton.hasClass('d-none')) {
                    subscribeButton.addClass('d-none')
                }
            } else if (state != 'granted') {
                subscribeButton.removeClass('d-none')
                if (!notificationsBlocked.hasClass('d-none')) {
                    notificationsBlocked.addClass('d-none')
                }
            } else {
                if (!notificationsBlocked.hasClass('d-none')) {
                    notificationsBlocked.addClass('d-none')
                }
                if (!subscribeButton.hasClass('d-none')) {
                    subscribeButton.addClass('d-none')
                }
            }
        });
}

function subscribeUserToPush() {
    if ('serviceWorker' in navigator) {
        return navigator.serviceWorker.register('service-worker.js')
            .then(function (registration) {
                const subscribeOptions = {
                    userVisibleOnly: true,
                    applicationServerKey: urlBase64ToUint8Array(
                        $("meta[name='_webpush_pubkey']").attr("content")
                    )
                };

                return registration.pushManager.subscribe(subscribeOptions);
            })
            .then(function (pushSubscription) {
                var csrfToken = $("meta[name='_csrf']").attr("content");
                var csrfHeader = $("meta[name='_csrf_header']").attr("content");
                var userEmail = $("meta[name='_user_email']").attr("content");

                // send subscription to the server side
                fetch('/notifications/' + userEmail + '/subscribe', {
                    method: 'POST',
                    credentials: 'same-origin',
                    body: JSON.stringify(pushSubscription),
                    headers: {
                        'content-type': 'application/json',
                        [csrfHeader]: csrfToken
                    }
                })
                    .catch(error => console.error('Error when sending subscription:', error))
                    .then(response => console.log('Successfully sent subscription'))

                return pushSubscription;
            })
            .finally(() => toggleNotificationControls());
    }
}