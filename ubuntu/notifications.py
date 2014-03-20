import pynotify,sys, gtk


def notify_sms(sender, content, sender_image=None):
    if not pynotify.init("jMessage"):
        sys.exit(1)
        # handle missing pynotify better
    notification = pynotify.Notification(sender, content)
    if sender_image:
        pbl = gtk.gdk.PixbufLoader()
        pbl.write(sender_image)
        pbl.close()
        icon = pbl.get_pixbuf()
        notification.set_icon_from_pixbuf(icon)
    if not notification.show():
        sys.exit(1)
        # handle errors better
