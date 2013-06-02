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
            if(data.resumeMoveRow == -1){
                $scope.currentGame.figureSelectedRow = -1;
                $scope.currentGame.figureSelectedCol = -1;
                $scope.currentGame.availableMoves = null;
                $scope.currentGame.canPassMove = false;
            }else{
                $scope.currentGame.figureSelectedRow = data.resumeMoveRow;
                $scope.currentGame.figureSelectedCol = data.resumeMoveCol;
                $scope.currentGame.availableMoves = data.moves;
                $scope.currentGame.canPassMove = true;
                console.log('can pass move');
            }

            if($scope.currentGame.currentMove==$scope.currentGame.yourcolor){
                $timeout.cancel(updatepageTimer);
                updatepageTimer = null;
                console.log("timer stop");
            }else{
                if(updatepageTimer == null && $scope.currentGame.gameStatus == 2){
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

    $scope.selectField = function(row, col){
        if($scope.currentGame){
            if($scope.currentGame.gameField [row][col] == $scope.currentGame.currentMove &&
               $scope.currentGame.currentMove == $scope.currentGame.yourcolor &&
               $scope.currentGame.canPassMove == false){
                if($scope.currentGame.figureSelectedRow != -1){
                    $scope.currentGame.figureSelectedRow = -1;
                    $scope.currentGame.figureSelectedCol = -1;
                }else{
                    var getAvailableMoveReq = {
                        action: "getavailablemove",
                        parameters: {
                            gameid: $scope.currentGame.id,
                            row: row,
                            col: col
                        }
                    };
                    $http.get("/checkersapi?json=" + JSON.stringify(getAvailableMoveReq)).success(function(data) {
                        if(data.moves.length > 0){
                            $scope.currentGame.figureSelectedRow = row;
                            $scope.currentGame.figureSelectedCol = col;
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
                        console.log('onmove');
                        if(data.resumeMoveRow == -1){
                            $scope.currentGame.figureSelectedRow = -1;
                            $scope.currentGame.figureSelectedCol = -1;
                            $scope.currentGame.availableMoves = null;
                            $scope.currentGame.canPassMove = false;
                        }else{
                            $scope.currentGame.figureSelectedRow = data.resumeMoveRow;
                            $scope.currentGame.figureSelectedCol = data.resumeMoveCol;
                            $scope.currentGame.availableMoves = data.moves;
                            $scope.currentGame.canPassMove = true;
                        }
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
    $scope.finishMove =function(){
        var doPassMoveReq = {
            action: "passmove",
            parameters: {
                gameid:  $scope.currentGame.id
            }
        };
        
        $http.get("/checkersapi?json=" + JSON.stringify(doPassMoveReq)).success(function(data) {
            $scope.currentGame = data;
            if(data.resumeMoveRow == -1){
                $scope.currentGame.figureSelectedRow = -1;
                $scope.currentGame.figureSelectedCol = -1;
                $scope.currentGame.availableMoves = null;
                $scope.currentGame.canPassMove = false;
            }else{
                $scope.currentGame.figureSelectedRow = data.resumeMoveRow;
                $scope.currentGame.figureSelectedCol = data.resumeMoveCol;
                $scope.currentGame.availableMoves = data.moves;
                $scope.currentGame.canPassMove = true;
            }

            if($scope.currentGame.currentMove==$scope.currentGame.yourcolor){
                $timeout.cancel(updatepageTimer);
                updatepageTimer = null;
                console.log("timer stop");
            }else{
                if(updatepageTimer == null && $scope.currentGame.gameStatus == 2){
                    console.log("timer start");
                    updatepageTimer = $timeout($scope.onUpdatePage,2000);
                }
            }
        });
    }
    
    $scope.onUpdatePage = function(){
        console.log("ontimer");
        if($scope.currentGame && $scope.currentGame.gameStatus == 2){
            $scope.SelectGame($scope.currentGame.id)
            updatepageTimer = $timeout($scope.onUpdatePage,2000);
        }else{
            updatepageTimer = null;
        }
    }
}

