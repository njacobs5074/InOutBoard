'use strict';
/**
 * Created by highlandcows on 29/11/14.
 */
angular
    .module('InOutBoardApp', ['ngResource', 'ngMaterial'])
    .factory('InOutBoardStatusValues', function ($resource) {
        return $resource('/inoutboard-rest/user-status-values/');
    })
    .factory('InOutBoardUser', function ($resource) {
        return $resource('/inoutboard-rest/user/:handle', null, {
            'update': {method: 'PUT'}
        });
    })
    .factory('InOutBoardUsers', function ($resource) {
        return $resource('/inoutboard-rest/get-all-users/');
    })
    .factory('InOutBoardUserStatus', function ($resource) {
        return $resource('/inoutboard-rest/user-status-update/');
    })
    .service('InOutUpdateService', ['$q', '$timeout', function ($q, $timeout) {

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

    }])
    .service('InOutUserService', ['$rootScope', 'InOutBoardStatusValues', 'InOutBoardUsers', 'InOutUpdateService', function ($rootScope, InOutBoardStatusValues, InOutBoardUsers, InOutUpdateService) {

        var service = {
            EVENT_ID: 'userData.update',

            userData: {
                userInfo: {handle: '', name: '', inOutBoardStatus: '', loggedIn: false},
                statusValues: {},
                inOutUsers: []
            },

            setUserInfo: function (handle, name, loggedIn) {
                service.userData.userInfo.handle = handle;
                service.userData.userInfo.name = name;
                service.userData.userInfo.loggedIn = loggedIn;
                service.userData.userInfo.inOutBoardStatus = loggedIn ? 'Registered' : 'Unregistered';

                $rootScope.$broadcast(service.EVENT_ID);
            }
        };

        // Initialize the list of users from the server.
        // Note that we use the #query interface because it defaults to array processing for
        // the results.
        var initialize = function () {

            // Load the existing list of users into our model.
            InOutBoardUsers.query(function (data) {
                service.userData.inOutUsers = data;
            });

            // Initialize the list of status values from the server.  Again, we use the
            // #query interface because it handles arrays automatically for us.
            InOutBoardStatusValues.query(function (data) {
                service.userData.statusValues = data;
            });

            // Register to receive messages - This implementation is tied
            // to the way AngularJS $q promises work.  See the implementation of the
            // InOutUpdateService#receive and the AngularJS documentation.
            InOutUpdateService.receive().then(null, null, function (message) {
                console.log('InOutUserService: got an update: ' + message.handle);

                // We just loop through all the users and if we find the one that matches
                // based on the 'handle' field, we either unregister or update it as
                // specified by the 'inOutBoardStatus' field.
                var foundUser = false;
                for (var i = 0; i < service.userData.inOutUsers.length; i++) {

                    if (service.userData.inOutUsers[i].handle === message.handle) {

                        if (message.inOutBoardStatus === 'Unregistered') {
                            console.log('Removing ' + message.handle);
                            service.userData.inOutUsers.remove(i);
                        }
                        else {
                            console.log('Updating ' + message.handle);
                            service.userData.inOutUsers[i].inOutBoardStatus = message.inOutBoardStatus;
                            service.userData.inOutUsers[i].name = message.name;
                            service.userData.inOutUsers[i].comment = message.comment;
                            service.userData.inOutUsers[i].lastUpdated = new Date(message.lastUpdated);

                            // Keep track of our own status this way.
                            if (message.handle === service.userData.userInfo.handle) {
                                service.userData.userInfo.inOutBoardStatus = message.inOutBoardStatus;
                            }
                        }

                        foundUser = true;
                        break;
                    }
                }

                // Must a user we haven't seen yet.
                if (!foundUser) {
                    if (message.inOutBoardStatus !== 'Unregistered') {
                        console.log('Adding ' + message.handle);
                        var userData = message;
                        userData.lastUpdated = new Date(message.lastUpdated);
                        service.userData.inOutUsers.push(userData);
                    }
                }

                // Let any listeners know that we've updated the data model.
                $rootScope.$broadcast(service.EVENT_ID);
            });

            // Just in case there are any listeners who've already attached to our data model.
            $rootScope.$broadcast(service.EVENT_ID);
        };

        initialize();

        return service;
    }])
    .controller('HandleLoginDialogController', ['$scope', '$mdDialog', '$mdToast', 'InOutBoardUser', 'InOutUserService', function ($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService) {
        return HandleLoginDialogController($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService);
    }])
    .controller('InOutBoardLoginDialogController', ['$scope', '$mdDialog', 'InOutBoardUser', 'InOutUserService', function ($scope, $mdDialog, InOutBoardUser, InOutUserService) {
        return InOutBoardLoginDialogController($scope, $mdDialog, InOutBoardUser, InOutUserService);
    }])
    .controller('InOutBoardLogoutDialogController', ['$scope', '$mdDialog', '$mdToast', 'InOutBoardUser', 'InOutUserService', function ($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService) {
        return InOutBoardLogoutDialogController($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService);
    }])
    .controller('InOutBoardStatusUpdateDialogController', ['$scope', '$mdDialog', 'InOutUserService', 'InOutBoardUserStatus', function ($scope, $mdDialog, InOutUserService, InOutBoardUserStatus) {
        return InOutBoardStatusUpdateDialogController($scope, $mdDialog, InOutUserService, InOutBoardUserStatus);
    }])
    .controller('HandleStatusUpdateDialogController', ['$scope', '$mdDialog', '$mdToast', 'InOutUserService', 'InOutBoardUserStatus', function ($scope, $mdDialog, $mdToast, InOutUserService, InOutBoardUserStatus) {
        return HandleStatusUpdateDialogController($scope, $mdDialog, $mdToast, InOutUserService, InOutBoardUserStatus);
    }])
    .controller('InOutBoardCtrl', ['$scope', '$mdDialog', 'InOutBoardStatusValues', 'InOutBoardUsers', 'InOutUserService', function ($scope, $mdDialog, InOutBoardStatusValues, InOutBoardUsers, InOutUserService) {

        // Listen for updates from our InOutUserService and forward those to our view model.
        $scope.$on(InOutUserService.EVENT_ID, function () {
            $scope.userData = InOutUserService.userData;
        });

        // Initialize our view model.
        $scope.userData = InOutUserService.userData;
    }]);