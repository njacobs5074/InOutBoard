'use strict';
/**
 * Created by highlandcows on 17/12/14.
 */
describe('Web Services Mock', function () {

    var InOutBoardStatusValues,
        mockStatusResponse = ['Registered', 'Unregistered'];

    var InOutBoardUser,
        mockUserResponse = {
            type: 'UserStatusUpdateMessage',
            handle: 'njacobs5074',
            inOutBoardStatus: 'Registered',
            name: 'Nick Jacobs',
            comment: '',
            lastUpdated: 1418938596031
        };

    beforeEach(function () {
        InOutBoardStatusValues = {
            query: function (callback) {
                callback(mockStatusResponse);
            }
        };

        InOutBoardUser = {
            query: function (callback) {
                callback(mockUserResponse);
            }
        }
    });

    it('InOutBoardStatusValues.query', function () {
        var results = undefined;
        InOutBoardStatusValues.query(function (data) {
            results = data;
        });
        expect(results).toEqual(mockStatusResponse);
    });

    it('InOutBoardUser.query', function () {
        var results = undefined;
        InOutBoardUser.query(function (data) {
            results = data;
        });
        expect(results).toEqual(mockUserResponse);
    });
});