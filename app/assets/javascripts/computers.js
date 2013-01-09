var computerApp = {
    constant: {},
    controller: {},
    serviceFactory: {}
}

computerApp.controller.ListCtrl = function($scope, $location, computers, urlParameters) {
    var update = function(data) {
        $scope.page = data;
        $scope.isFirstPage = ($scope.page.page == 0);
        $scope.isLastPage = ($scope.page.offset + $scope.page.items.length == $scope.page.total);
    };
    $scope.search = $location.search().f;

    update(computers);

    $scope.find = function() {
        $location.search(urlParameters(0, $scope.search));
    };

    $scope.nextPage = function() {
        $location.search("p", $scope.page.page + 1)
    };
    $scope.prevPage = function() {
        $location.search("p", $scope.page.page - 1)
    }
};

computerApp.constant.urlParameters =  function(page, search) {
    var params = {};

    if (search) {
        params.f = search;
    }
    if (page !== undefined) {
        params.p = page;
    }
    return params;
};

computerApp.controller.ListCtrl.resolve = {
    computers: function(Computer, $location, urlParameters, resolveService) {
        var search = $location.search().f;

        var params = urlParameters($location.search().p, search);

        return resolveService.resourcePromise(Computer.query, params);
    }
};

computerApp.serviceFactory.resolveService = function($q) {
    return {
        resourcePromise: function(method, params) {
            var deferred = $q.defer();
            method(params, function(data) {
                deferred.resolve.call(deferred, data);
            }, function() {
                deferred.reject.call(deferred, arguments);
            });

            return deferred.promise;
        }
    }
}

computerApp.controller.CreateCtrl = function($scope, $location, Computer, Company) {
    Company.query(function(companies) {
        $scope.companies = companies;
    });
    $scope.save = function() {
        Computer.save($scope.computer, function(computer) {
            $location.path("/");
        })
    }
}

computerApp.controller.EditCtrl = function($scope, $location, $routeParams, Computer, Company) {
    var self = this;

    Computer.get({id: $routeParams.id}, function(computer) {
        self.original = computer;
        self.original.id = undefined;
        $scope.computerId = $routeParams.id;
        $scope.computer = new Computer(self.original);
    });

    Company.query(function(companies) {
        $scope.companies = companies;
    });

    $scope.isClean = function() {
        return angular.equals(self.original, $scope.computer);
    };

    $scope.destroy = function() {
        self.original.$remove({id: $routeParams.id}, function() {
            $location.path("/")
        });
    };

    $scope.save = function() {
        $scope.computer.$update({id: $routeParams.id}, function() {
            $location.path("/")
        });
    };
};

computerApp.controller.EditCtrl.resolve = {
    computer: function($routeParams, Computer) {
        return Computer.get({id: $routeParams.id}, function(computer) {

        })
    }
}

computerApp.serviceFactory.Computer = function($resource) {
    var Computer = $resource('/api/computers/:id', {},
        {
            'query':  {method: 'GET', isArray: false},
            'update': {method: 'PUT'}
        });
    return Computer;
};

computerApp.serviceFactory.Company = function($resource) {
    return $resource('/api/companies/:id')
};

angular.module('application', ['ngResource']).
    config(function($routeProvider) {
        $routeProvider.
            when('/',           {redirectTo: '/list'}).
            when('/list',       {controller: 'ListCtrl', templateUrl:'list.html', resolve: computerApp.controller.ListCtrl.resolve}).
            when('/new',        {controller: 'CreateCtrl', templateUrl:'detail.html'}).
            when('/edit/:id',   {controller: 'EditCtrl', templateUrl:'detail.html'}).
            otherwise({redirectTo:'/'});
    }).
    config(function($locationProvider) {
        $locationProvider.html5Mode(false)
    }).
    constant(computerApp.constant).
    filter('undef', function() {
        return function(input) {
            if (input == undefined) {
                return "-"
            } else {
                return input
            }
        }
    }).
    controller(computerApp.controller).
    factory(computerApp.serviceFactory).
    run(function($rootScope) {
        $rootScope.$on("$routeChangeError", function(event, current, previous, rejected) {
            console.log(event);
            console.log(current);
            console.log(previous);
            console.log(rejected);
            event.currentScope.error = "We have an error"
        });
        $rootScope.$on("$routeChangeSuccess", function(event, current, previous, rejected) {
            console.log(event);
            console.log(current);
            console.log(previous);
            console.log(rejected);
            event.currentScope.error = undefined;
        });
    })
;



