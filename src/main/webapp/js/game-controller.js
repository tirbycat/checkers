function DataCtrl($scope, $routeParams, $http, $timeout) {
    var updatepageTimer = null;
    var getGamesReq = {
        action: "getusergames",
        parameters: {}
    };

    $http.get("/checkersapi?json=" + JSON.stringify(getGamesReq), $routeParams).success(function(data) {
        $scope.mygames = data.mygames;
        $scope.joinedgames = data.joinedgames;
    });

    $scope.addNewGame = function(){
        var addNewGameReq = {
            action: "newgame",
            parameters: {}
        };
        $http.get("/checkersapi?json=" + JSON.stringify(addNewGameReq)).success(function(data) {
            $scope.mygames[$scope.mygames.length] = data.gameid;
        });
    }

    $scope.SelectGame = function(gameid){
        var selectGameReq = {
            action: "getgame",
            parameters: {
                gameid: gameid
            }
        };

        $http.get("/checkersapi?json=" + JSON.stringify(selectGameReq)).success(function(data) {
            $scope.currentGame = data;
            $scope.currentGame.figureSelectedRow = -1;
            $scope.currentGame.figureSelectedCol = -1;

            if($scope.currentGame.currentMove==$scope.currentGame.yourcolor){
                $timeout.cancel(updatepageTimer);
                updatepageTimer = null;
                console.log("timer stop");
            }else{
                if(updatepageTimer == null){
                    console.log("timer start");
                    updatepageTimer = $timeout($scope.onUpdatePage,2000);
                }
            }
        });
    }

    if(window.loadGameId){
        $scope.SelectGame(loadGameId)
    }

    $scope.joinTheGame = function(gameid){
        var joinTheGameReq = {
            action: "jointhegame",
            parameters: {
                gameid: gameid
            }
        };

        $http.get("/checkersapi?json=" + JSON.stringify(joinTheGameReq)).success(function(data) {
            $scope.joinedgames[$scope.joinedgames.length] = data.id;
            $scope.SelectGame (data.id);
        });
    }

    $scope.isSelected = function(i,j){
        if($scope.currentGame && $scope.currentGame.figureSelectedRow != -1 &&
           $scope.currentGame.figureSelectedCol != -1){
            if($scope.currentGame.figureSelectedRow == i &&
                $scope.currentGame.figureSelectedCol == j){
                return 'selected';
            }
        }
        return '';
    }

    $scope.selectField = function(i, j){
        if($scope.currentGame){
            if($scope.currentGame.gameField [i][j] == $scope.currentGame.currentMove &&
               $scope.currentGame.currentMove == $scope.currentGame.yourcolor){
                if($scope.currentGame.figureSelectedRow != -1){
                    $scope.currentGame.figureSelectedRow = -1;
                    $scope.currentGame.figureSelectedCol = -1;
                }else{
                    var getAvailableMoveReq = {
                        action: "getavailablemove",
                        parameters: {
                            gameid: $scope.currentGame.id,
                            i: i,
                            j: j
                        }
                    };
                    $http.get("/checkersapi?json=" + JSON.stringify(getAvailableMoveReq)).success(function(data) {
                        if(data.moves.length > 0){
                            $scope.currentGame.figureSelectedRow = i;
                            $scope.currentGame.figureSelectedCol = j;
                            $scope.currentGame.availableMoves = data.moves;
                        }else{
                            $scope.currentGame.availableMoves = null;
                        }
                    });
                }
            }
        }
    }

    $scope.selectMove = function(row, col){
        if($scope.currentGame.figureSelectedRow != -1 && 
            $scope.currentGame.availableMoves != null){
            for(var i=0;i<$scope.currentGame.availableMoves.length;i++){
                console.log($scope.currentGame.availableMoves [i]);
                if($scope.currentGame.availableMoves [i].row == row &&
                   $scope.currentGame.availableMoves [i].col == col){

                   var doMoveReq = {
                        action: "domove",
                        parameters: {
                            gameid:  $scope.currentGame.id,
                            rowFrom: $scope.currentGame.figureSelectedRow,
                            colFrom: $scope.currentGame.figureSelectedCol,
                            rowTo:   row,
                            colTo:   col
                        }
                    };

                    $http.get("/checkersapi?json=" + JSON.stringify(doMoveReq)).success(function(data) {
                        $scope.currentGame.gameField = data.gameField;
                        $scope.currentGame.currentMove = data.currentMove;
                        $scope.currentGame.gameStatus = data.gameStatus;
                        $scope.currentGame.figureSelectedRow = -1;
                        $scope.currentGame.figureSelectedCol = -1;
                        $scope.currentGame.availableMoves = null;
                        if($scope.currentGame.currentMove!=$scope.currentGame.yourcolor){
                            updatepageTimer = $timeout($scope.onUpdatePage,2000);
                            console.log("timer start");
                        }
                    });

                    return;
                }
            }
        }
    }
    $scope.onUpdatePage = function(){
        console.log("ontimer");
        if($scope.currentGame){
            $scope.SelectGame($scope.currentGame.id)
            updatepageTimer = $timeout($scope.onUpdatePage,2000);
        }else{
            updatepageTimer = null;
        }
    }
}

