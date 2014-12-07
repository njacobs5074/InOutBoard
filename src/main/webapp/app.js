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

        var userData = {
            userInfo: {handle: '', name: '', loggedIn: false},
            statusValues: {},
            inOutUsers: []
        };

        var service = {
            EVENT_ID: 'userData.update',

            // Store the data in the root scope so that it is available to all UI controllers.
            getUserData: function () {
                return userData;
            },

            setUserInfo: function(handle, name, loggedIn) {
                userData.userInfo.handle = handle;
                userData.userInfo.name = name;
                userData.userInfo.loggedIn = loggedIn;

                $rootScope.$broadcast(service.EVENT_ID);
            }
        };

        // Initialize the list of users from the server.
        // Note that we use the #query interface because it defaults to array processing for
        // the results.
        var initialize = function () {

            // Load the existing list of users into our model.
            InOutBoardUsers.query(function (data) {
                userData.inOutUsers = data;
            });

            // Initialize the list of status values from the server.  Again, we use the
            // #query interface because it handles arrays automatically for us.
            InOutBoardStatusValues.query(function (data) {
                userData.statusValues = data;
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
                for (var i = 0; i < userData.inOutUsers.length; i++) {

                    if (userData.inOutUsers[i].handle === message.handle) {

                        if (message.inOutBoardStatus === 'Unregistered') {
                            console.log('Removing ' + message.handle);
                            userData.inOutUsers.remove(i);
                        }
                        else {
                            console.log('Updating ' + message.handle);
                            userData.inOutUsers[i].inOutBoardStatus = message.inOutBoardStatus;
                            userData.inOutUsers[i].name = message.name;
                            userData.inOutUsers[i].comment = message.comment;
                        }

                        foundUser = true;
                        break;
                    }
                }

                // Must a user we haven't seen yet.
                if (!foundUser) {
                    if (message.inOutBoardStatus !== 'Unregistered') {
                        console.log('Adding ' + message.handle);
                        userData.inOutUsers.push(message);
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
    .controller('LoginDialogController', ['$scope', '$mdDialog', '$mdToast', 'InOutBoardUser', 'InOutUserService', function ($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService) {
            return LoginDialogController($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService);
        }])
    .controller('InOutBoardLoginDialogController', ['$scope', '$mdDialog', 'InOutBoardUser', 'InOutUserService', function ($scope, $mdDialog, InOutBoardUser, InOutUserService) {
        return InOutBoardLoginDialogController($scope, $mdDialog, InOutBoardUser, InOutUserService);
    }])
    .controller('InOutBoardLogoutDialogController', ['$scope', '$mdDialog', '$mdToast', 'InOutBoardUser', 'InOutUserService', function ($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService) {
            return InOutBoardLogoutDialogController($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService);
        }])
    .controller('InOutBoardCtrl', ['$scope', '$mdDialog', 'InOutBoardStatusValues', 'InOutBoardUsers', 'InOutUserService', function ($scope, $mdDialog, InOutBoardStatusValues, InOutBoardUsers, InOutUserService) {

        // Listen for updates from our InOutUserService and forward those to our view model.
        $scope.$on(InOutUserService.EVENT_ID, function () {
            $scope.userData = InOutUserService.getUserData();
        });

        // Initialize our view model.
        $scope.userData = InOutUserService.getUserData();
    }]);