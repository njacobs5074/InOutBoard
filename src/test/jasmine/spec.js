'use strict';
/**
 * Created by highlandcows on 17/12/14.
 */
describe('Web Services Mock', function () {

    var InOutBoardStatusValuesMock,
        mockStatusResponse = ['Registered', 'Unregistered'];

    var InOutBoardUsersMock,
        mockUserResponse = {
            type: 'UserStatusUpdateMessage',
            handle: 'njacobs5074',
            inOutBoardStatus: 'Registered',
            name: 'Nick Jacobs',
            comment: '',
            lastUpdated: 1418938596031
        };

    var InOutUpdateServiceMock, listener;
    var $q, $rootScope, localStorageMock = {};

    var InOutUserServiceMock;

    var receivedMessage;

    // Need to manually specify this for tests.
    beforeEach(function () {
        module('ngMock');
    });

    // These are standard AngularJS components that we use.
    beforeEach(inject(function (_$q_, _$rootScope_) {
        $q = _$q_;
        $rootScope = _$rootScope_;
    }));

    // Set up our mock services.
    beforeEach(function () {
        InOutBoardStatusValuesMock = {
            query: function (callback) {
                callback(mockStatusResponse);
            }
        };

        InOutBoardUsersMock = {
            query: function (callback) {
                callback([mockUserResponse]);
            }
        };

        listener = $q.defer();
        InOutUpdateServiceMock = {
            receive: function () {
                return listener.promise;
            },

            mockReceiveMessage: function (json) {
                listener.notify(JSON.parse(json));
            }
        };

        var storage = {};
        localStorageMock = {
            getItem: function (key) {
                return storage[key];
            },
            setItem: function (key, value) {
                storage[key] = value + '';
                return storage[key];
            },
            clear: function () {
                storage = {};
            }
        };

        InOutUserServiceMock = InOutUserService($rootScope, localStorageMock, InOutBoardStatusValuesMock, InOutBoardUsersMock, InOutUpdateServiceMock);

        receivedMessage = undefined;
    });

    afterEach(function () {
        receivedMessage = undefined;
        localStorageMock.clear();
    });

    // Sanity checks for web service mocks.
    it('InOutBoardStatusValues.query', function () {
        var results = undefined;
        InOutBoardStatusValuesMock.query(function (data) {
            results = data;
        });
        expect(results).toEqual(mockStatusResponse);
    });

    it('InOutBoardUser.query', function () {
        var results = undefined;
        InOutBoardUsersMock.query(function (data) {
            results = data;
        });
        expect(results).toEqual([mockUserResponse]);
    });

    it('InOutUpdateService.receive', function () {
        InOutUpdateServiceMock.receive().then(null, null, function (message) {
            receivedMessage = message;
        });

        InOutUpdateServiceMock.mockReceiveMessage(JSON.stringify(mockUserResponse));

        $rootScope.$digest();
        expect(receivedMessage).not.toBe(undefined);
    });

    // Test a version of the InOutUserService that uses the mock services.
    // This is arguably the real test of the suite.
    it('InOutUserService.initialize', function () {
        InOutUpdateServiceMock.receive().then(null, null, function (message) {
            receivedMessage = message;
        });
        InOutUpdateServiceMock.mockReceiveMessage(JSON.stringify(mockUserResponse));
        InOutUserServiceMock.setUserInfo(mockUserResponse.handle, mockUserResponse.name, true);

        expect(InOutUserServiceMock.userData.userInfo.handle).toEqual(mockUserResponse.handle);
        expect(InOutUserServiceMock.userData.userInfo.name).toEqual(mockUserResponse.name);
        expect(InOutUserServiceMock.userData.userInfo.loggedIn).toBeTruthy();

        expect(InOutUserServiceMock.userData.inOutUsers[0].handle).toEqual(mockUserResponse.handle);
        expect(InOutUserServiceMock.userData.inOutUsers[0].name).toEqual(mockUserResponse.name);
        expect(InOutUserServiceMock.userData.inOutUsers[0].inOutBoardStatus).toEqual('Registered');
    })
});