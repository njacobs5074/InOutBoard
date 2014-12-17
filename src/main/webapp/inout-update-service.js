/**
 * Created by highlandcows on 17/12/14.
 */
function InOutUpdateService($q, $timeout) {

    var service = {
        receive: function () {
            return listener.promise;
        }
    };

    var RECONNECT_TIMEOUT = 3 * 1000;
    var SOCKET_URL = '/inoutboard-websocks';
    var UPDATE_TOPIC = '/topic/user-status-update';

    // This service uses WebSockets/STOMP to receive asynchronous updates from
    // the server.
    var listener = $q.defer();
    var socket = {
        client: null,
        stomp: null
    };

    var reconnect = function () {
        $timeout(function () {
            console.log('Attempting to reconnect to server...');
            initialize();
        }, RECONNECT_TIMEOUT);
    };

    // Grab the body of the message and deserialize it to a JS object.
    var getMessage = function (data) {
        return JSON.parse(data.body);
    };

    var startListener = function () {
        socket.stomp.subscribe(UPDATE_TOPIC, function (data) {
            listener.notify(getMessage(data));
        });
    };

    var initialize = function () {
        socket.client = new SockJS(SOCKET_URL);
        socket.stomp = Stomp.over(socket.client);
        socket.stomp.connect({}, startListener, reconnect);
    };

    initialize();

    return service;

}
