/**
 * Created by highlandcows on 07/12/14.
 */
/**
 * Controller for the login dialog. Basically we just expose a method
 * that displays the login-dialog-content.html in a dialog panel.
 */
function InOutBoardLoginDialogController($scope, $mdDialog, InOutBoardUser, InOutUserService) {

    $scope.showLoginDialog = showLoginDialog;

    function showLoginDialog(event) {
        $mdDialog.show({
            targetEvent: event,
            templateUrl: 'login-dialog-content.html',
            controller: HandleLoginDialogController,
            locals: {InOutBoardUser: InOutBoardUser, InOutUserService: InOutUserService}
        })
    }
}

/**
 * Controller class that handles the user's interaction with the login dialog - see above.
 * Here we connect to the server with the credentials the user provided.
 */
function HandleLoginDialogController($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService) {

    var successfulLogin = function () {
        $mdToast.show($mdToast.simple().content('Successfully logged in: ' + $scope.handle + ',' + $scope.name));
        InOutUserService.setUserInfo($scope.handle, $scope.name, true);
    };

    var unsuccessfulLogin = function (reason) {
        $mdToast.show($mdToast.simple().content('Failed to login: ' + reason.statusText + ' (' + reason.status + ')'));;
    };

    $scope.cancelLoginDialog = function () {
        InOutUserService.setUserInfo('', '', false);
        $mdDialog.cancel();
    };

    $scope.okayLoginDialog = function () {
        InOutBoardUser.update({handle: $scope.handle}, {
            handle: $scope.handle,
            name: $scope.name
        }).$promise.then(successfulLogin, unsuccessfulLogin);

        $mdDialog.hide();
    }
}

/**
 * Controller that manages a dialog that allows the user to disconnect from the app.  Since this is
 * basically a yes/no question, we can use a confirm dialog.
 */
function InOutBoardLogoutDialogController($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService) {

    $scope.showLogoutDialog = showLogoutDialog;

    function showLogoutDialog(event) {

        var confirm = $mdDialog.confirm()
            .targetEvent(event)
            .title('Logout')
            .content('Disconnect from In/Out Board?')
            .ok('Logout')
            .cancel('Cancel');

        $mdDialog.show(confirm).then(function () {
            InOutBoardUser.delete({handle: InOutUserService.userData.userInfo.handle}, function () {
                InOutUserService.setUserInfo('', '', false);
                $mdToast.show($mdToast.simple().content('Logged out'));
            });
        });

    }
}

/**
 * Controller for update status dialog.  Basically we just expose a method that displays the
 * status-dialog-content.html in a dialog panel.
 */
function InOutBoardStatusUpdateDialogController($scope, $mdDialog, InOutUserService, InOutBoardUserStatus) {
    $scope.showStatusUpdateDialog = showStatusUpdateDialog;

    function showStatusUpdateDialog(event) {
        $mdDialog.show({
            targetEvent: event,
            templateUrl: 'status-dialog-content.html',
            controller: HandleStatusUpdateDialogController,
            locals: {InOutUserService: InOutUserService, InOutBoardUser: InOutBoardUserStatus}
        })
    }
}

function HandleStatusUpdateDialogController($scope, $mdDialog, $mdToast, InOutUserService, InOutBoardUserStatus) {

    $scope.statusValues = InOutUserService.userData.statusValues;

    var successfulUpdate = function () {
        $mdToast.show($mdToast.simple().content('Updated status to: ' + InOutUserService.userData.userInfo.inOutBoardStatus));
    };

    $scope.cancelStatusUpdate = function () {
        $mdDialog.cancel();
    };

    $scope.okayStatusUpdate = function () {
        InOutBoardUserStatus.save({
            handle: InOutUserService.userData.userInfo.handle,
            name: InOutUserService.userData.userInfo.name,
            inOutBoardStatus: $scope.selectedStatus,
            comment: $scope.comment
        }).$promise.then(successfulUpdate);

        $mdDialog.hide();
    }
}

