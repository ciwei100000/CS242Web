<!DOCTYPE html>
<html lang="en" >
<title>Twitter Search Enginee 1.0</title>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <title>登录</title>
  <script src="./node_modules/angular/angular.js"></script>
  <meta name="Description" content="A tweet enginee search front end part">
  <link rel="stylesheet" href="assets/styles.css">
  <link href='http://fonts.googleapis.com/css?family=Oswald' rel='stylesheet' type='text/css'>
</head>
<body ng-app="myApp">

<section id="wrapper" ng-controller="SearchEngine">
  <h1>Twitter Search Enginee 1.0</h1>
  <div id="main">
      <form name="MyForm">
          <input type="text" id="search" ng-model="search">
          <input type="submit" class="solid" value="Search by Hadoop" ng-click="submithadoop()" >
          <input type="submit" class="solid" value="Search by Lucene" ng-click="submitlucene()">
      </form>
  </div>


</section>
<script>
    var app = angular.module("myApp", []);

    app.controller("SearchEngine", function($scope,$http) {

            $scope.submitlucene=function()
            {
                var hre = 'LuceneIndex.html?query=' + $scope.search;
                location.href=hre;
            };
            $scope.submithadoop=function () {
                var hre = 'HadoopIndex.html?query=' + $scope.search;
                location.href=hre;
            }
        });
</script>
</body>
</html>