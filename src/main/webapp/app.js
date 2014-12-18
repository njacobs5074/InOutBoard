'use strict';
/**
 * Created by highlandcows on 29/11/14.
 */
angular
    .module('InOutBoardApp', ['ngResource', 'ngMaterial', 'ngStorage', 'ngMock'])
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
        return InOutUpdateService($q, $timeout);
    }])
    .service('InOutUserService', ['$rootScope', '$localStorage', 'InOutBoardStatusValues', 'InOutBoardUsers', 'InOutUpdateService', function ($rootScope, $localStorage, InOutBoardStatusValues, InOutBoardUsers, InOutUpdateService) {
        return InOutUserService($rootScope, $localStorage, InOutBoardStatusValues, InOutBoardUsers, InOutUpdateService);
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