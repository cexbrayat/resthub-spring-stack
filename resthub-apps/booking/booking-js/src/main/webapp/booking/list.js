define([ 'resthub.controller', 'repositories/booking.repository' ], function(Controller) {

	return Controller.extend("ListBookingsController", {
		template : 'booking/list.html',
		init : function() {
			var user = $.storage.getJSONItem('user');
			if (user.id) {
				BookingRepository.read($.proxy(this, '_displayBookings'), 'user/' + user.id);
			}
		},
		_displayBookings : function(bookings) {
			this.render({
				bookings : bookings
			});
		}
	});

});