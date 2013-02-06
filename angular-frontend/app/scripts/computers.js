var computerApp = {
    constant: {},
    controller: {},
    provider: {},
    value: {},
    serviceFactory: {}
}

computerApp.controller.ListCtrl = function($scope, $location, computers, urlParameters) {
    var update = function(data) {
        $scope.page = data;
        $scope.isFirstPage = ($scope.page.page == 0);
        $scope.isLastPage = ($scope.page.offset + $scope.page.items.length == $scope.page.total);
    };
    $scope.search = $location.search().f;

    $scope.sort = { column: $location.search().s, descending: $location.search().d };

    $scope.head = [
        {name: 'Computer name', column: 1, class: {'col2': true}},
        {name: 'Introduced', column: 2, class: {'col3': true}},
        {name: 'Discontinued', column: 3, class: {'col4': true}},
        {name: 'Company', column: 4, class: {'col5': true}}
    ];

    $scope.changeSorting = function(column) {
        var sort = $scope.sort;
        if (sort.column == column) {
            sort.descending = !sort.descending;
        } else {
            sort.column = column;
            sort.descending = false;
        }
        var search = $location.search()
        search.s = sort.column;
        search.d = sort.descending;
        $location.search(search);
    };

    $scope.selectedCls = function(column) {
        var boolean2Sort = { false: 'sort-down', true: 'sort-up' }
        return column == $scope.sort.column && boolean2Sort[$scope.sort.descending];
    };

    $scope.isSortColumn = function(column) {
        return $scope.sort.column == column
    }
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

computerApp.constant.urlParameters =  function(page, search, sort, desc) {
    var params = {};

    if (search) {
        params.f = search;
    }
    if (page !== undefined) {
        params.p = page;
    }
    if (sort !== undefined) {
        params.s = sort;
    }
    if (desc !== undefined) {
        params.d = desc;
    }
    return params;
};

computerApp.constant.baseUrl = '' //http://localhost:9001\:9001'

computerApp.controller.ListCtrl.resolve = {
    computers: function(Computer, $location, urlParameters, resolveService) {
        var search = $location.search().f;

        var params = urlParameters($location.search().p, search, $location.search().s, $location.search().d);

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

computerApp.controller.LoginCtrl = function($scope, $location, Login, User, $q, RetryHttpService) {

    $scope.login = function() {
        Login.save($scope.user, function() {
            hide();
            RetryHttpService.retryRequests();
        });
    };

    $scope.$on('event:loginRequired', function() {
        if ($scope.user) {
            $scope.user.password = '';
        }
        show();
    });

    function show() {
        $('#login-modal').modal('show');
    }

    function hide() {
        $('#login-modal').modal('hide');
    }
};

computerApp.controller.NavCtrl = function($scope, User, Login, $rootScope) {
    $scope.user = User.get();

    $scope.username = function() {
        return User.get().username;
    }

    $scope.isLogin = function() {
        return $scope.username();
    }
    $scope.getClass = function() {
        return '';
    }
    $scope.login = function() {
        $rootScope.$broadcast('event:loginRequired');
    };
    $scope.logout = function() {
        Login.delete({ username: User.get().username });
    };


}

computerApp.controller.EditCtrl.resolve = {
    computer: function($routeParams, Computer) {
        return Computer.get({id: $routeParams.id}, function(computer) {

        })
    }
}

computerApp.serviceFactory.Computer = function($resource, baseUrl) {
    var Computer = $resource(baseUrl + '/api/computers/:id', {},
        {
            'query':  {method: 'GET', isArray: false},
            'update': {method: 'PUT'}
        });
    return Computer;
};

computerApp.serviceFactory.Login = function($resource, baseUrl) {
    var Login = $resource(baseUrl + '/api/login/:username', {username: '@username'});
    return Login;
};

computerApp.provider.RetryHttpService = function() {
    var requests401 = [];
    this.interceptor = function($rootScope, $q, flash) {
        function success(response) {
            return response;
        }

        function error(response) {
            var status = response.status;

            if (status == 401) {
                var deferred = $q.defer();
                var req = {
                    config: response.config,
                    deferred: deferred
                }
                requests401.push(req);
                $rootScope.$broadcast('event:loginRequired');
                return deferred.promise;
            }
            // otherwise
            flash.add({message: "Server error: " + status,  longMessage: response.data})
            return $q.reject(response);

        }

        return function(promise) {
            return promise.then(success, error);
        }
    };

    this.$get =  function($http) {
        return {
            retryRequests: function() {
                var i, requests = requests401;
                for (i = 0; i < requests.length; i++) {
                    retry(requests[i]);
                }
                requests401 = [];

                function retry(req) {
                    $http(req.config).then(function(response) {
                        req.deferred.resolve(response);
                    });
                }
            }
        };
    };

}

computerApp.serviceFactory.Company = function($resource, baseUrl) {
    return $resource(baseUrl + '/api/companies/:id')
};

computerApp.serviceFactory.User = function($cookieStore) {
    var USER_NAME_KEY = 'LOGGED_IN_USER_COMPUTER_APP';
    return {
        get: function() {
            var cookie = $cookieStore.get(USER_NAME_KEY);
            if (cookie) {
                return cookie.data;
            } else {
                return { };
            }
        }
    }
}

computerApp.controller.FlashCtrl = function($scope, flash) {
    $scope.flashes = flash.getAll()

    $scope.$on('flash.add', function(event, flash) {
        $scope.flashes.push(flash)
    });
}

computerApp.serviceFactory.flash = function($rootScope) {
    var flashes = [];

    return {

        /**
         * add adds a single flash message.
         *
         * @param message
         *  A string representing the flash message
         * @param level
         *  the classification of the flash options are:
         *  - 'info' // the default
         *  - 'success'
         *  - 'error'
         */
        add: function (message, level) {
            // default value for the level parameter
            level = level || 'info';

            var flash = {
                message: message,
                level: level
            };
            flashes.push(flash);

            // tell child scope that this flash has been added
            $rootScope.$broadcast('flash.add', flash);
        },

        /**
         * all returns all flashes, but does **not** clear them
         * @return {Array}
         */
        all: function () {
            return flashes;
        },

        /**
         * clear removes all flashes
         */
        clear: function () {
            $rootScope.$broadcast('flash.clear', true);
            flashes = [];
        },

        /**
         * getAll returns all flashes and clears them
         *
         * @return {Array}
         */
        getAll: function () {
            $rootScope.$broadcast('flash.remove');
            var f = angular.copy(flashes);
            flashes = [];
            return f;
        }
    };
};

angular.module('application', ['ngResource', 'ngCookies', 'ngSanitize']).
    config(function($routeProvider) {
        $routeProvider.
            when('/',           {redirectTo: '/list'}).
            when('/list',       {controller: 'ListCtrl', templateUrl:'partials/list.html', resolve: computerApp.controller.ListCtrl.resolve}).
            when('/new',        {controller: 'CreateCtrl', templateUrl:'partials/detail.html'}).
            when('/edit/:id',   {controller: 'EditCtrl', templateUrl:'partials/detail.html'}).
            when('/login',   {controller: 'LoginCtrl', templateUrl:'partials/login.html'}).
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
    provider(computerApp.provider).
    value(computerApp.value).
    config(function($httpProvider, RetryHttpServiceProvider) {
        $httpProvider.responseInterceptors.push(RetryHttpServiceProvider.interceptor)
    }).
    run(function($rootScope) {
        $rootScope.$on("$routeChangeError", function(event, current, previous, rejected) {
            event.currentScope.error = "We have an error"
        });
        $rootScope.$on("$routeChangeSuccess", function(event, current, previous, rejected) {
            event.currentScope.error = undefined;
        });
    })
;



