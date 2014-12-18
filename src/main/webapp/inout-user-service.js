'use strict';
/**
 * Created by highlandcows on 17/12/14.
 */
function InOutUserService($rootScope, $localStorage, InOutBoardStatusValues, InOutBoardUsers, InOutUpdateService) {

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

            $localStorage.userInfo = service.userData.userInfo;

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

        // See if the user has been saved into local storage.  If so,
        // then initialize from that.
        if ($localStorage.userInfo) {
            service.userData.userInfo = $localStorage.userInfo;
        }

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
}