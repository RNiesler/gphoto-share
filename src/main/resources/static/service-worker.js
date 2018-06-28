self.addEventListener('push', function (event) {
    if (event.data) {
        console.log('Received notification')
        const notification = event.data.json()
        const title = notification.title
        const options = {
            body: notification.description,
            data: notification.url,
            requireInteraction: true,
            actions: [
                {action: 'open-album', title: 'Open the Album', icon: '/icons/gphoto.png'},
                {action: 'open-app', title: 'Open the Application', icon: '/icons/camera.png'}
            ]
        }
        event.waitUntil(self.registration.showNotification(title, options))
    } else {
        console.warn('Unexpected notification with no payload.')
    }
});

self.addEventListener('notificationclick', function (event) {
    var url = event.notification.data
    if (event.action == 'open-app') {
        url = self.registration.scope
    }
    event.waitUntil(clients.openWindow(url))
})