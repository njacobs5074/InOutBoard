/**
 * Created by highlandcows on 07/12/14.
 */
/**
 * AngularJS controller for the login dialog controller. Basically we just expose a method
 * that displays the login-dialog-content.html in an AngularJS Material Design dialog.
 */
function InOutBoardLoginDialogController($scope, $mdDialog, InOutBoardUser, InOutUserService) {

    $scope.showLoginDialog = showLoginDialog;

    function showLoginDialog(event) {
        $mdDialog.show({
            targetEvent: event,
            templateUrl: 'login-dialog-content.html',
            controller: LoginDialogController,
            locals: {InOutBoardUser: InOutBoardUser, InOutUserService: InOutUserService}
        })
    }
}

/**
 * Controller class that handles the user's interaction with the login dialog - see above.
 * Here we connect to the server with the credentials the user provided.
 */
function LoginDialogController($scope, $mdDialog, $mdToast, InOutBoardUser, InOutUserService) {

    var successfulLogin = function () {
        $mdToast.show($mdToast.simple().content('Successfully logged in: ' + $scope.handle + ',' + $scope.name));
        InOutUserService.setUserInfo($scope.handle, $scope.name, true);
    };

    $scope.cancelLoginDialog = function () {
        InOutUserService.setUserInfo('', '', false);
        $mdDialog.cancel();
    };

    $scope.okayLoginDialog = function () {
        InOutBoardUser.update({handle: $scope.handle}, {
            handle: $scope.handle,
            name: $scope.name
        }).$promise.then(successfulLogin);

        $mdDialog.hide();
    }
}

/**
 * Controller that manages a dialog that allows the user to disconnect from the app.
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
            InOutBoardUser.delete({handle: InOutUserService.getUserData().userInfo.handle}, function () {
                InOutUserService.setUserInfo('', '', false);
                $mdToast.show($mdToast.simple().content('Logged out'));
            });
        });

    }
}