'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('CategoriesController', function ($scope, $q, $interval, $modal, $log, $cookies, DataService, TextMessages, PortletService) {

        var loading = false, reload = false, loadTimeoutId = null, loadDeferred = null;

        var svgs = [];
        $scope.roots = [];
        $scope.hierarchies = {};
        $scope.treeTypes = {};
        var scale = 1;

        $scope.hostGroups = [];
        $scope.serviceGroups = [];

        $scope.scaleUp = function () {
            scale += 0.05;

            if(scale > 3) {
                scale = 3;
            }

            var centerX = 0, centerY = 0,
                container = $("#canvas-container"),
                canvas = $("#canvas-inner");

            var windowLeft = container.scrollLeft();
            var windowTop = container.scrollTop();

            var windowW = container.width() / scale;
            var windowH = container.height() / scale;

            centerX = ((windowLeft + windowW) + windowLeft) / 2;
            centerY = ((windowTop + windowH) + windowTop) / 2;

            canvas.css({transform: "scale(" + scale + "," + scale + ")", "transform-origin": centerX + " " + centerY, width: "100%", height: "100%"});

            setTimeout(function()
            {
                canvas.css({width: "", height: ""});
            },
            1);
        };

        $scope.scaleDown = function () {
            scale -= 0.05;

            if(scale < 0.2) {
                scale = 0.2;
            }

            var centerX = 0, centerY = 0,
                container = $("#canvas-container"),
                canvas = $("#canvas-inner");

            var windowLeft = container.scrollLeft();
            var windowTop = container.scrollTop();

            var windowW = container.width() / scale;
            var windowH = container.height() / scale;

            centerX = ((windowLeft + windowW) + windowLeft) / 2;
            centerY = ((windowTop + windowH) + windowTop) / 2;

            canvas.css({transform: "scale(" + scale + "," + scale + ")", "transform-origin": centerX + " " + centerY, width: "100%", height: "100%"});            
            
            setTimeout(function()
            {
                canvas.css({width: "", height: ""});
            },
            1);
        };

        $("#canvas-container").css({
            width: $("#canvas-container").width() + "px",
            height: ($(window).height() - $("#canvas-container").offset().top - 80) + "px"
        });

        $scope.alerts = [
        ];

        $scope.closeAlert = function (index) {
            $scope.alerts = [];
        };

        $scope.addFailureAlert = function (errorMessage, status) {
            $scope.alerts.length = 0;
            var statusMsg = (status === undefined) ? "none" : status;
            $scope.alerts.push({type: 'danger', msg: TextMessages.get('serverFailure', errorMessage, statusMsg) });
        };

        $scope.loadingMessage = false;

        function correctGraphHeight(index)
        {
            //for(var i = 0, iLimit = svgs.length; i < iLimit; i++)
            //{
                var svg  = svgs[index][0][0],
                    svg_ = $(svg),
                    div  = svg_.parent();

                var maxY = 0, minY = 0, x = 0, hasEntities = false;

                svg_.find("g.node").each(function()
                {
                    var self = $(this),
                        transform = self.attr("transform");

                    var coords = [];

                    if(!transform.length)
                    {
                        coords = ['0', '0'];
                    }
                    else
                    {
                        var t1 = transform.split("(")[1];
                        var t2 = t1.split(")")[0] || 0;

                        coords = t2.split(/[\,\s]/);
                    }

                    var y = parseFloat((coords[1] || 0), 10);
                    
                    if(maxY < y)
                        maxY = y;

                    if(minY > y)
                        minY = y;

                    var x_ = self.find("text").attr("x");

                    if(!y && !parseFloat((coords[0] || 0), 10) && x_ && (x_.charAt(0) == "-"))
                    {
                        var rect = this.getBoundingClientRect();

                        if(rect && rect.width)
                        {
                            x = rect.width / scale;
                        }
                        else
                        {
                            rect = this.getBBox();

                            if(rect)
                            {
                                x = rect.width / scale;
                            }
                        }
                    }
                    
                    if(self.children().length > 2)
                        hasEntities = true;
                });

                var bboxHeight = (maxY + 15 + Math.abs(minY) + 10) + 4 + (hasEntities ? 10 : 0);

                svg_.css({height: bboxHeight + "px"});

                try
                {
                    var root_ = svg_.find("g.root").get(0);

                    svg_.css({width: ((((root_.getBoundingClientRect().width || root_.getBBox().width) / scale) || 0) + 50) + "px"});
                }
                catch(e) {};

                div.css({height: bboxHeight + "px"});

                $(svg.firstChild)
                    .css({transform: "translate(" + Math.ceil(x || 25) + "px," + (Math.ceil(Math.abs(minY)) + 15 + 2) + "px)"});

                svg.firstChild.setAttribute('transform', 'matrix(1 0 0 1 ' + Math.ceil(x || 25) + ' ' + (Math.ceil(Math.abs(minY)) + 15 + 2) + ')');
            //}
        }

        $scope.init = function ()
        {
            var parseTreeForEntries = function(root)
            {
                if(root.entities && root.entities.length)
                {
                    for(var i = 0, iLimit = root.entities.length; i < iLimit; i++)
                    {
                        // Should we add entities as roots?
                        //if(($scope.showHostGroups || $scope.showServiceGroups))
                        {
                            if(!root.children) {
                                root.children = [];
                            }

                            if(!root.children_) {
                                root.children_ = [];
                            }

                            root.fakeChildren = true;

                            for(var i = 0, iLimit = root.entities.length; i < iLimit; i++)
                            {
                                var entity = root.entities[i], name = "",
                                    type = (entity.entityTypeName || (entity.hosts ? "HOSTGROUP" : (entity.service ? "SERVICE_GROUP" : "")));

                                switch(type)
                                {
                                    case "HOSTGROUP":
                                        for(var j = 0, jLimit = $scope.hostGroups.length; j < jLimit; j++)
                                        {
                                            var hostGroup = $scope.hostGroups[j];

                                            if(hostGroup.id == entity.objectID)
                                            {
                                                name = hostGroup.name;
                                                break;
                                            }
                                        }

                                        break;

                                    case "SERVICE_GROUP":
                                        for(var j = 0, jLimit = $scope.serviceGroups.length; j < jLimit; j++)
                                        {
                                            var serviceGroup = $scope.serviceGroups[j];

                                            if(serviceGroup.id == entity.objectID)
                                            {
                                                name = serviceGroup.name;
                                                break;
                                            }
                                        }
                                    
                                        break;
                                }

                                root.children_.push({id: entity.objectID, name: name, root: false, entity: true, entityTypeName: type, entityType: {
                                    "id" : 24,
                                    "name" : type,
                                    "description" : "com.groundwork.collage.model.impl.CustomGroup",
                                    "isLogicalEntity" : true,
                                    "applicationTypeSupported" : false
                                },
                                children: [], children_: [], childNames: [], parents: [root]});

                                if(((type == "HOSTGROUP") && !$scope.showHostGroups) || ((type == "SERVICE_GROUP") && !$scope.showServiceGroups))
                                {
                                    continue;
                                }

                                root.children.push({id: entity.objectID, name: name, root: false, entity: true, entityTypeName: type, entityType: {
                                    "id" : 24,
                                    "name" : type,
                                    "description" : "com.groundwork.collage.model.impl.CustomGroup",
                                    "isLogicalEntity" : true,
                                    "applicationTypeSupported" : false
                                },
                                children: [], children_: [], childNames: [], parents: [root]});
                            }
                        }
                    }
                }

                if(root.children && root.children.length)
                {
                    for(var j = 0, jLimit = root.children.length; j < jLimit; j++)
                    {
                        parseTreeForEntries(root.children[j]);
                    }
                }

                return false;
            };

            // skip initialization load if concurrently loading
            if (loading && !reload) {
                reload = true;
                if (loadTimeoutId != null) {
                    clearTimeout(loadTimeoutId);
                }
                if (loadDeferred != null) {
                    loadDeferred.reject();
                }
                return;
            }

            // set initialization loading status
            $scope.loadingMessage = "";
            loading = true;
            reload = false;
            // clear svg canvas
            $("#canvas-inner").html("");
            svgs = [];
            // load category hierarchy roots, service groups, and host groups
            DataService.getCategoryHierarchyRoots('CUSTOM_GROUP').then(function (hierarchyRoots) {
                if (reload) {
                    return $q.reject();
                }
                $scope.roots = hierarchyRoots.categories;
                return DataService.getServiceGroups();
            }).then(function (serviceGroups) {
                if (reload) {
                    return $q.reject();
                }
                $scope.serviceGroups = serviceGroups.serviceGroups;
                return DataService.getHostGroups();
            }).then(function (hostGroups) {
                if (reload) {
                    return $q.reject();
                }
                $scope.hostGroups = hostGroups.hostGroups;
                // sequentially load category hierarchies
                $scope.hierarchies = {};
                var chain = $q.when();
                var count = 0;
                _.forEach($scope.roots, function (root) {
                    chain = chain.then(function () {
                        $scope.loadingMessage = ++count + " of " + $scope.roots.length;
                        return DataService.getCategory(root.name, root.entityTypeName).then(function (category) {
                            if (reload) {
                                return $q.reject();
                            }
                            return DataService.getCategoryHierarchy(category);
                        }).then(function (categoryHierarchy) {
                            if (reload) {
                                return $q.reject();
                            }
                            $scope.hierarchies[categoryHierarchy.name] = categoryHierarchy;
                        });
                    });
                });
                return chain;
            }).then(function () {
                if (reload) {
                    return $q.reject();
                }
                // sequentially show loaded hierarchies
                $scope.loadingMessage = false;
                var chain = $q.when();
                _.forEach($scope.roots, function (root, index) {
                    chain = chain.then(function () {
                        if (reload) {
                            return $q.reject();
                        }
                        var categoryHierarchy = $scope.hierarchies[root.name];
                        parseTreeForEntries(categoryHierarchy);
                        $scope.showHierarchy(categoryHierarchy);
                        loadDeferred = $q.defer();

                        loadTimeoutId = setTimeout(function () {
                            correctGraphHeight(index);
                            loadDeferred.resolve();
                        }, 1100);

                        return loadDeferred.promise;
                    });
                });
                return chain;
            }).catch(function(error) {
                // log unexpected errors
                if (!!error) {
                    console.log("Unexpected initialization error: "+error);
                }
            }).finally(function() {
                // cleanup and reload if requested during previous load
                loadTimeoutId = null;
                loadDeferred = null;
                loading = false;
                if (reload) {
                    reload = false;
                    $scope.init($scope.readResourceURL);
                }
            });
        }; // end init

        $scope.showHostsView = function()
        {
            var modalInstance = $modal.open({
                templateUrl: '/portal-groundwork-base/app/views/modals/hosts-management.html',
                controller: HostsManagementController,
                backdrop: true,
                resolve: {
                    DataService: function() { return DataService; }
                }
            });

            modalInstance.result.finally(function () {
                refresh();
            });
        };

        $scope.showServiceGroupView = function()
        {
            var modalInstance = $modal.open({
                templateUrl: '/portal-groundwork-base/app/views/modals/service-management.html',
                controller: ServiceGroupsManagementController,
                backdrop: true,
                resolve: {
                    DataService: function() { return DataService; }
                }
            });

            modalInstance.result.finally(function () {
                refresh();
            });
        };

        function refresh() {
            $scope.init($scope.readResourceURL);
        }

        /* ***** */

        var margin = {top: 0, right: 0, bottom: 20, left: 100},
            width = document.body.clientWidth - margin.right - margin.left,
            height = document.body.clientHeight - margin.top - margin.bottom,
            dragoffset = {top: 0, left: 0};

        var i = 0,
            duration = 250,
            rootNodes = {},
            dragTimer;

        var diagonal = d3.svg.diagonal()
            .projection(function(d) { return [d.y, d.x]; });

        //

        function clearDragTimer() { dragTimer = null; }

        var dragData = null, dragNode = null, dragStartTime = 0, closest = null;

        var updateTempConnector = function(closestNode, draggingNode)
        {
            var data = [], allowed = true;

            if (draggingNode && closestNode)
            {
                // have to flip the source coordinates since we did this for the existing connectors on the original tree
                data = [ {source: {x: closestNode.y + margin.left, y: closestNode.x},
                          target: {x: draggingNode.y + margin.left, y: draggingNode.x} } ];

                var dragTypeName = getTreeEntityTypeName(draggingNode, true),
                    treeTypeName = getTreeEntityTypeName(closestNode);

                // can only drop on a custom group node
                allowed = allowed &&
                    (closestNode.entityTypeName === 'CUSTOM_GROUP');
                // can only drop on a custom or matching type group tree hierarchy
                allowed = allowed &&
                    ((treeTypeName === 'CUSTOM_GROUP') || (dragTypeName === 'CUSTOM_GROUP') || (treeTypeName === dragTypeName));
                // can only drop if matching custom group's children type
                allowed = allowed &&
                    (_.isEmpty(closestNode.entities) || (draggingNode.entityTypeName === closestNode.entities[0].entityTypeName)) &&
                    (_.isEmpty(closestNode.children) || (draggingNode.entityTypeName === closestNode.children[0].entityTypeName)) &&
                    (_.isEmpty(closestNode.children_) || (draggingNode.entityTypeName === closestNode.children_[0].entityTypeName));
            }

            var link = d3.select("#canvas-inner").selectAll(".templink").data(data);

            link.enter().append("path")
                .attr("d", d3.svg.diagonal() )
                .attr('pointer-events', 'none');

            link.attr("d", d3.svg.diagonal())
                .attr("class", "templink" + (!allowed ? " disabled" : ""))

            link.exit()
                .remove();
        }

        var drag = d3.behavior.drag()
            .on("dragstart", function(d) {
                closest = null;
                dragNode = null;

                var thisNode = $(d3.select(this).node());
                dragStartTime = new Date().getTime();

                setTimeout(function()
                {
                    if(dragStartTime && d && d.name) {
                        dragData = d;
                        dragStartTime = new Date().getTime();

                        var dragCanvas = d3.select("#canvas-inner").append("svg").attr("class", "drag-canvas");
                        dragCanvas.node().appendChild(thisNode.clone().get(0));

                        var rect = thisNode.get(0).getBoundingClientRect(),
                            canvasObj = dragNode = $(dragCanvas[0]),
                            top_ = thisNode.position().top / scale,
                            left_ = thisNode.position().left / scale;

                        if(dragNode.offsetParent().get(0).id != "canvas-inner")
                        {
                            top_ -= $("#canvas-inner").offset().top / scale/* + Math.ceil(rect.height / scale)*/;
                            left_ -= $("#canvas-inner").offset().left / scale/* + Math.ceil(rect.width / scale)*/;
                        }

                        canvasObj.css({width: Math.ceil(rect.width / scale) + "px", height: Math.ceil(rect.height / scale) + "px", top: top_ + "px", left: left_ + "px"});

                        if(thisNode.find("text").attr("x").charAt(0) == "-")
                        {
                            canvasObj.children(":eq(0)")
                                .css("transform", "translate(" + (Math.ceil(rect.width / scale) - 11) + "px," + Math.ceil(rect.height / scale / 2) + "px)")
                                .attr("transform", "matrix(1 0 0 1 " + (Math.ceil(rect.width / scale) - 11) + " " + Math.ceil(rect.height / scale / 2) + ")");
                        }
                        else
                        {
                            canvasObj.children(":eq(0)")
                                .css("transform", "translate(11px," + Math.ceil(rect.height / scale / 2) + "px)")
                                .attr("transform", "matrix(1 0 0 1 11 " + Math.ceil(rect.height / scale / 2) + ")");
                        }

                        return;
                    }

                    if(d3.event && d3.event.sourceEvent)
                        d3.event.sourceEvent.stopPropagation();
                },
                250);
            })
            .on("drag", function(d)
            {
                var dx = d3.event.dx;
                var dy = d3.event.dy;

                if(dragNode) {
                    var y1 = parseFloat(dragNode.css("top"), 10) + dy / scale;
                    var x1 = parseFloat(dragNode.css("left"), 10) + dx / scale;

                    dragNode.css({
                        top: y1 + "px",
                        left: x1 + "px"
                    });

                    /*
                    var links = d3.select("#canvas-inner").selectAll("path.link").filter(function(d_) {
                        return ((d_.source === dragData.parent && d_.target === dragData) || (d_.source === dragData));
                    });

                    links.remove();

                    dragNode.attr( 'pointer-events', 'none' );

                    d3.select(dragNode.node().parentNode).attr("transform", "translate(" + dragData.y + "," + dragData.x + ")");
*/
                    var nodes = d3.select("#canvas-inner").selectAll("g.node")[0], minDist, closestNode,
                        prevClosestName = closest ? closest.name : '',
                        prevClosestEntityTypeName = closest ? closest.entityTypeName : '';

                    for(var i = 0, iLimit = nodes.length; i < iLimit; i++)
                    {
                        var data = d3.select(nodes[i]).data()[0];

                        if(!data)
                            continue;

                        if(data.entityTypeName != "CUSTOM_GROUP")
                            continue;

                        var node_ = $(nodes[i]),
                            icon = node_.find("polygon:eq(0), circle:eq(0)");

                        var iconPos = icon.position(),
                            y2 = iconPos.top / scale, x2 = iconPos.left / scale;

                        if(dragNode.offsetParent().get(0).id != "canvas-inner")
                        {
                            y2 -= $("#canvas-inner").offset().top / scale;
                            x2 -= $("#canvas-inner").offset().left / scale;
                        }

                        var dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

                        if(typeof minDist == 'undefined') {
                            minDist = dist;
                            closest = data;
                            closestNode = node_;
                        }
                        else {
                            if(minDist > dist) {
                                minDist = dist;
                                closest = data;
                                closestNode = node_;
                            }
                        }
                    }

                    if((closest.name !== prevClosestName) || (closest.entityTypeName !== prevClosestEntityTypeName))
                    {
                        var current = $("#canvas-inner g.node.glow");
                    
                        if(current.length)
                        {
                            var classes = $("#canvas-inner g.node.glow").attr("class").split(" ");
                            classes.splice(classes.indexOf("glow"), 1);
                        
                            current.attr("class", classes.join(" "));
                        }

                        if((dragData.name !== closest.name) || (dragData.entityTypeName !== closest.entityTypeName))
                            closestNode.attr("class", closestNode.attr("class") + " glow");
                    }
/*
                    updateTempConnector(closest, dragData);

                    return;
                }

                dragoffset.left += dx;
                dragoffset.top  += dy;

                d3.select("#canvas-inner").attr("transform", "translate(" + dragoffset.left + "," + dragoffset.top + ")scale(" + scale + ")");

                if(dragTimer)
                    clearTimeout(dragTimer);

                dragTimer = setTimeout(clearDragTimer, 500);*/
                }
            })
            .on("dragend", function() {
                if(!dragNode)
                    return;

                var current = $("#canvas-inner g.node.glow");
                    
                if(current.length)
                {
                    var classes = $("#canvas-inner g.node.glow").attr("class").split(" ");
                    classes.splice(classes.indexOf("glow"), 1);
                        
                    current.attr("class", classes.join(" "));
                }

                if(!dragData)
                    return;

                dragNode.remove();

                if(!closest)
                    return;

                if((closest.name === dragData.name) && (closest.entityTypeName === dragData.entityTypeName))
                    return;

                var dragTypeName = getTreeEntityTypeName(dragData, true),
                    treeTypeName = getTreeEntityTypeName(closest),
                    allowed = true;

                // can only drop on a custom group node
                allowed = allowed &&
                    (closest.entityTypeName === 'CUSTOM_GROUP');
                // can only drop on a custom or matching type group tree hierarchy
                allowed = allowed &&
                    ((treeTypeName === 'CUSTOM_GROUP') || (dragTypeName === 'CUSTOM_GROUP') || (treeTypeName === dragTypeName));
                // can only drop if matching custom group's children type
                allowed = allowed &&
                    (_.isEmpty(closest.entities) || (dragData.entityTypeName === closest.entities[0].entityTypeName)) &&
                    (_.isEmpty(closest.children) || (dragData.entityTypeName === closest.children[0].entityTypeName)) &&
                    (_.isEmpty(closest.children_) || (dragData.entityTypeName === closest.children_[0].entityTypeName));

                if(closest && allowed)
                {
                    switch(dragData.entityTypeName)
                    {
                        case 'HOSTGROUP':
                        case 'SERVICE_GROUP':
                            DataService.deleteCategoryEntities(dragData.parent, [dragData.objectId], dragData.entityTypeName)
                                .then(function()
                                {
                                    DataService.addCategoryEntities(closest, [dragData.objectId], dragData.entityTypeName)
                                        .then(function()
                                        {
                                            refresh();
                                        });
                                });

                            break;

                        case 'CUSTOM_GROUP':
                            DataService.addCategory(dragData, closest, true)
                                .then(function()
                                {
                                    refresh();
                                });

                            break;
                    }
                }
            });

        //

        //d3.select("#canvas-inner").call(drag);

        $scope.story = [];

        $scope.showHostGroups = ($cookies.showHostGroups == "true") || false;
        if($scope.showHostGroups)
            $("#show-hg-root").attr("checked", "checked");
        
        $scope.showServiceGroups = ($cookies.showServiceGroups == "true") || false;
        if($scope.showServiceGroups)
            $("#show-sg-root").attr("checked", "checked");
        
        $scope.showRoots = function(type)
        {
            switch(type)
            {
                case "HOSTGROUP":
                    $scope.showHostGroups = !$scope.showHostGroups;
                    break;

                case "SERVICE_GROUP":
                    $scope.showServiceGroups = !$scope.showServiceGroups;
                    break;
            }

            refresh();
            
            $cookies.showHostGroups = $scope.showHostGroups;
            $cookies.showServiceGroups = $scope.showServiceGroups;
        };

        $scope.addRoot = function(type)
        {
            var modalInstance = $modal.open({
                templateUrl: '/portal-groundwork-base/app/views/modals/category-detail.html',
                controller: CategoryDetailsInstanceController,
                backdrop: false,
                resolve: {
                    DataService: function() { return DataService; },
                    category: function() { return {
                        name: '',
                        type: type,
                        description: ''
                    }; }
                }
            });

            modalInstance.result.then(function (result) {
                DataService.createRootCategory({
                    name: result.name,
                    description: result.description,
                    entityTypeName: result.type
                }).then(function success()
                {
                    refresh();
                },
                function error() {});
            }, function () {
            });
        };

        $scope.showHierarchy = function(root)
        {
            var tree = d3.layout.tree()
                .nodeSize([40, 30]);

            var svg = d3.select("#canvas-inner").append("div")
                .attr("class", "svg-container")
                .append("svg")
                .attr("class", "graph");

            svgs.push(svg);

            svg.append("g")
               .attr("class", "root")
               .attr("id", "root-" + root.name.replace(/\s/g, ''));

            var treeData = preprocessTree(root);

            var rootNode = treeData[0];
            rootNode.x0 = 0;
            rootNode.y0 = 0;

            rootNodes[root.name] = [rootNode, tree, rootNode, svg];

            update(rootNode, tree, rootNode, svg);
        };

        var ops = 
        {
            "Edit": [],
            "Create": [
                "Custom Group"
            ],
            "Delete": [
                "Group",
                "Group and make children root nodes",
                "Group and attach children to parent"
            ],
            "Other": [
                "Detach and Make Root"
            ]
        };

        function buildMenu()
        {
            var menu = "<ul id=\"contextmenu\">";

            for(var op in ops)
            {
                menu += "<li><a href=\"#\">" + op + "</a>";

                var inner = ops[op];

                if(inner.length)
                    menu += "<ul class=\"submenu\">";

                for (var i = 0, iLimit = inner.length; i < iLimit; i++)
                {
                    menu += "<li><a href=\"#\">" + inner[i] + "</a></li>";
                }
                
                if(inner.length)
                    menu += "</ul>";

                menu += "</li>"
            }

            menu += "</ul>";

            return $(menu);
        }

        function preprocessTree(gwTree)
        {
            $scope.treeTypes[gwTree.name] = gwTree.entityTypeName;

            var processNode = function(nodeSrc, nodeDest, parentName)
            {
                var name = nodeDest.name = nodeSrc.name;

                nodeDest.objectId = nodeSrc.id;
                nodeDest.entityTypeName = nodeSrc.entityTypeName;
                
                if(nodeSrc.entities && nodeSrc.entities.length)
                {
                    if(nodeSrc.entities[0].entityTypeName == "HOSTGROUP")
                    {
                        nodeDest.hasEntity = "HOSTGROUP";
                    }
                    else if(nodeSrc.entities[0].entityTypeName == "SERVICE_GROUP")
                    {
                        nodeDest.hasEntity = "SERVICE_GROUP";
                    }
                }

                nodeDest.parent = parentName;

                if((nodeDest.entityTypeName != $scope.treeTypes[gwTree.name]) && (nodeDest.entityTypeName != 'CUSTOM_GROUP'))
                    $scope.treeTypes[gwTree.name] = gwTree.entityTypeName;

                if(nodeSrc.root)
                    nodeDest.root = true;

                if(nodeSrc.children && nodeSrc.children.length)
                {
                    var childCount = nodeSrc.children.length;

                    nodeDest.children = new Array(childCount);

                    for(var i = 0; i < childCount; i++)
                    {
                        nodeDest.children[i] = {};

                        processNode(nodeSrc.children[i], nodeDest.children[i], name);
                    }
                }
                else
                {
                    if(nodeSrc.children_ && nodeSrc.children_.length)
                    {
                        var childCount = nodeSrc.children_.length;

                        nodeDest.children_ = new Array(childCount);

                        for(var i = 0; i < childCount; i++)
                        {
                            nodeDest.children_[i] = {};

                            processNode(nodeSrc.children_[i], nodeDest.children_[i], name);
                        }
                    }
                }
            };

            var ret = {};

            processNode(gwTree, ret, null);

            return [ret];
        }

function update(source, tree, root, svg)
{
  // Compute the new tree layout.
  var nodes = tree.nodes(root).reverse(),
      links = tree.links(nodes);

  // Normalize for fixed-depth.
  nodes.forEach(function(d) { d.y = d.depth * 290; });

  // Update the nodes...
  var rootNameSelector = escapeCSSSelector(root.name);
  var node = svg.select("#root-" + rootNameSelector).selectAll("g.node." + rootNameSelector)
	  .data(nodes, function(d)
      {
        return d.id || (d.id = ++i);
      });

  // Enter any new nodes at the parent's previous position.
  var nodeEnter = node.enter().append("g")
	  .attr("class", "node " + root.name.replace(/\s/g, ''))
	  .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; });

  nodeEnter.each(function(d)
  {
    var data = d3.select(this).data()[0],
        iconType, attrType;

    if(data.entityTypeName == "SERVICE_GROUP")
    {
        iconType = "circle";
        attrType = "r";
    }
    else
    {
        iconType = "polygon";
        attrType = "points";        
    }

    d3.select(this)
      .append(iconType)
	  .attr(attrType, function(d)
      {
            switch(d.entityTypeName)
            {
                case "HOSTGROUP":
                    d3.select(this.parentNode).append("polygon")
                        .attr("points", "-5,15 15,15 15,-5 -5,-5")
                        .attr("class", "hg entity");

                    d3.select(this.parentNode).append("polygon")
                        .attr("points", "0,20 20,20 20,0 0,0")
                        .attr("class", "hg entity");

                    return "-10,10 10,10 10,-10 -10,-10";

                case "SERVICE_GROUP":
                    d3.select(this.parentNode).append("circle")
                        .attr("r", "10")
                        .attr("transform", "translate(5, 5)")
                        .attr("class", "sg entity");

                    d3.select(this.parentNode).append("circle")
                        .attr("r", "10")
                        .attr("transform", "translate(10, 10)")
                        .attr("class", "sg entity");

                    return "10";
            }

          return "0,10 10,0 0,-10 -10,0";
      })
	  .attr("class", function(d)
      {
          var class_ = d.root && d.parent ? "clickable " : "";

          if(d.entityTypeName == "CUSTOM_GROUP")
          {
            if(d.hasEntity == "HOSTGROUP")
            {
              class_ += "hg";
            }
            else if(d.hasEntity == "SERVICE_GROUP")
            {
              class_ += "sg";
            }
            else
            {
              class_ += "cg";
            }
          }
          else
          {
            switch(d.entityTypeName)
            {
                case "HOSTGROUP":
                    class_ += "hg entity";
                    break;

                case "SERVICE_GROUP":
                    class_ += "sg entity";
                    break;
            }
          }

          return class_;
      });

      d3.select(this).call(drag);
  });
  
  nodeEnter.append("text")
	  .attr("x", function(d)
      {
          if(d.parent && d.children)
            return -8;

          if(d.children)
            return -15;

          switch(d.entityTypeName)
          {
            case "HOSTGROUP":
            case "SERVICE_GROUP":
              return 25;
          }

          return 15;
      })
	  .attr("dy", function(d)
      {
          if(d.parent && d.children)
              return -8;
         
          return 4;
      })
	  .attr("text-anchor", function(d) { return d.children ? "end" : "start"; })
      .attr("title", function(d) { return d.name; })
	  .text(function(d) { return (d.name.length > 40) ? (d.name.substr(0, 40) + "...") : d.name; })
	  .style("fill-opacity", 1e-6);

  nodeEnter.append("svg:title")
      .text(function(d) { return d.name; });

  // Transition nodes to their new position.
  var nodeUpdate = node.transition()
	  .duration(duration)
	  .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

  nodeUpdate.select("polygon, circle")
	  .attr("class", function(d)
      {
          var class_ = d.root && d.parent ? "clickable " : "";

          if(d.entityTypeName == "CUSTOM_GROUP")
          {
            if(d.hasEntity == "HOSTGROUP")
            {
              class_ += "hg";
            }
            else if(d.hasEntity == "SERVICE_GROUP")
            {
              class_ += "sg";
            }
            else
            {
              class_ += "cg";
            }
          }
          else
          {
            switch(d.entityTypeName)
            {
                case "HOSTGROUP":
                    class_ += "hg entity";
                    break;

                case "SERVICE_GROUP":
                    class_ += "sg entity";
                    break;
            }
          }

          return class_;
      })
      .each(function(d)
      {
            $(this).parent().click(function(event)
            {
                dragStartTime = null;
                showMenu(event, d);
            });
      });

  nodeUpdate.select("text")
	  .style("fill-opacity", 1);

  // Transition exiting nodes to the parent's new position.
  var nodeExit = node.exit().transition()
	  .duration(duration)
	  .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
	  .remove();

  nodeExit.select("text")
	  .style("fill-opacity", 1e-6);

  // Update the links:
  var link = svg.select("#root-" + rootNameSelector).selectAll("path.link")
	  .data(links, function(d) { return d.target.id; });

  // Enter any new links at the parent's previous position.
  link.enter().insert("path", "g")
	  .attr("class", "link")
	  .attr("d", function(d) {
		var o = {x: source.x0, y: source.y0};
		return diagonal({source: o, target: o});
	  });

  // Transition links to their new position.
  link.transition()
	  .duration(duration)
	  .attr("d", diagonal);

  // Transition exiting nodes to the parent's new position.
  link.exit().transition()
	  .duration(duration)
	  .attr("d", function(d) {
		var o = {x: source.x, y: source.y};
		return diagonal({source: o, target: o});
	  })
	  .remove();

  // Stash the old positions for transition.
  nodes.forEach(function(d) {
	d.x0 = d.x;
	d.y0 = d.y;

    var rt = getRootFromNode(d);
    
    if(rt)
    {
        d.rootName = rt.name;
    }
  });
}

function escapeCSSSelector(string) {
    return string.replace(/\s/g, '').replace(/([ !"#$%&'()*+,-./:;<=>?@[\\\]^`{|}~])/g, '\\$1').replace(/^([0-9])/,'\\3$1 ');
}

function getRootFromNode(d)
{
    if(d.root)
        return d;

    while(d.parent || !d.root) {
        d = d.parent;

        if(!d)
            return null;
    }

    return d;
}

function getTreeEntityTypeName(d, childrenOnly)
{
    d = childrenOnly ? d : getRootFromNode(d);

    var getBranchTypeName = function(d_)
    {
        if(d_.entityTypeName != "CUSTOM_GROUP")
            return d_.entityTypeName;

        if(d_.hasEntity && (d_.hasEntity != "CUSTOM_GROUP"))
            return d_.hasEntity;

        if(d_.entities && d_.entities.length && d_.entities[0] != "CUSTOM_GROUP")
            return d_.entities[0].entityTypeName;

        var childCount = (d_.children ? d_.children.length : 0),
            type = "CUSTOM_GROUP";

        for(var i = 0; i < childCount; i++)
        {
            var type_ = getBranchTypeName(d_.children[i]);

            if(type_ != "CUSTOM_GROUP")
            {
                type = type_;
                break;
            }
        }

        return type;
    };

    return getBranchTypeName(d);
}

function showMenu(e, d)
{   
    if(e.preventDefault)
        e.preventDefault();

    if(dragTimer)
        return;

    var entityTypeName = d.entityTypeName,
        treeEntityTypeName = getTreeEntityTypeName(d);

    var contextmenu = $("#contextmenu");

    if(contextmenu.length)
        contextmenu.remove();

    var menu = buildMenu();

    if(entityTypeName == 'CUSTOM_GROUP')
    {
        menu.find("li li:contains('Custom Group')").click(function()
        {
            addLeaf(d, 'CUSTOM_GROUP');
        });

        menu.find("li:contains('Edit')").click(function()
        {
            editLeaf(d);
        });

        // disable create if entities are not custom groups, (types of
        // custom group's children must match)
        var currentNode = findNodeInHierarchy(d.name);
        if (!!currentNode &&
            ((!_.isEmpty(currentNode.entities) && (currentNode.entities[0].entityTypeName != 'CUSTOM_GROUP')) ||
                (!_.isEmpty(currentNode.children) && (currentNode.children[0].entityTypeName != 'CUSTOM_GROUP')))) {
            menu.find("li > a:contains('Create')").remove();
        }
    }
    else
    {
            menu.find("li:contains('Edit')").click(function()
            {
                if(entityTypeName == 'HOSTGROUP')
                {
                    DataService.getHostGroup(d.name).then(function(host)
                    {
                        var modalInstance = $modal.open({
                            templateUrl: '/portal-groundwork-base/app/views/modals/group-detail.html',
                            controller: GroupDetailsInstanceController,
                            backdrop: false,
                            resolve: {
                                DataService: function() { return DataService; },
                                group: function() { return {
                                    id: host.id,
                                    name: host.name,
                                    alias: host.alias,
                                    description: host.description,
                                    typeName: host.appTypeDisplayName,
                                    hosts: host.hosts || []
                                } }
                            }
                        });

                        modalInstance.result.then(function (result)
                        {
                            DataService.createOrUpdateHostGroup({
                                id: host.id,
                                name: result.name,
                                alias: result.alias,
                                description: result.description,
                                hosts: result.hosts
                            })
                            .then(function()
                            {
                                refresh();
                            });
                        },
                        function () {
                        });
                    });
                }
                else if(d.entityTypeName == 'SERVICE_GROUP')
                {
                    DataService.getServiceGroup(d.name).then(function(host)
                    {
                        var modalInstance = $modal.open({
                            templateUrl: '/portal-groundwork-base/app/views/modals/service-group-detail.html',
                            controller: ServiceGroupDetailsInstanceController,
                            backdrop: false,
                            size: 'lg',
                            resolve: {
                                DataService: function() { return DataService; },
                                group: function() { return {
                                    id: host.id,
                                    name: host.name,
                                    description: host.description,
                                    typeName: host.appTypeDisplayName,
                                    services: host.services || []
                                } }
                            }
                        });

                        modalInstance.result.then(function (result)
                        {
                            DataService.createOrUpdateServiceGroup({
                                id: host.id,
                                name: result.name,
                                description: result.description,
                                services: result.services,
                                appType: (!!result.appType) ? result.appType : null
                            })
                            .then(function()
                            {
                                refresh();
                            });
                        },
                        function () {
                        });
                    });
                }
            });

            menu.find("li > a:contains('Create')").remove();

            var del = menu.find("li > a:contains('Delete')");
            del.closest("li").remove();

            menu.find("li > a:contains('Other')").parent().remove();
    }

    menu.find("li li:contains('Group')").filter(function() {
            return $(this).text() === "Group";
    })
    .click(function()
    {
        deleteLeaf(d);
    });

    menu.find("li li:contains('Group and make children root nodes')").click(function()
    {
        deleteCategory(d);
    });

    menu.find("li li:contains('Group and attach children to parent')").click(function()
    {
        deleteCategoryOnly(d);
    });

    menu.find("li li:contains('Detach and Make Root')").click(function()
    {
        setAsRoot(d);
    });

    menu.appendTo(document.body);

    start_v_menu();

    var node = $(e.target), offset = node.offset();
    menu.css({ top: (offset.top + 9) + "px", left: (offset.left + 9) + "px" });

    setTimeout(function()
    {
        $(document.body).on("click", function hideContextMenu(e)
        {
            menu.remove();

            $(document.body).off("click", hideContextMenu);
        });
    },
    200);

    return false;
}

function randomString(length_)
{
    var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz'.split('');

    if (typeof length_ !== "number") {
        length_ = Math.floor(Math.random() * chars.length_);
    }

    var str = '';
    for (var i = 0; i < length_; i++) {
        str += chars[Math.floor(Math.random() * chars.length)];
    }

    return str;
}

function addLeaf(d, type)
{
    var modalInstance = $modal.open({
        templateUrl: '/portal-groundwork-base/app/views/modals/category-detail.html',
        controller: CategoryDetailsInstanceController,
        backdrop: false,
        resolve: {
            DataService: function() { return DataService; },
            category: function() { return {
                name: '',
                type: type,
                description: ''
            } }
        }
    });

    modalInstance.result.then(function (result)
    {
        DataService.createCategory({
            name: result.name,
            description: result.description,
            entityTypeName: result.type
        }, d).then(function success()
        {
            /*
            if(!d.children) {
                d.children = [];
            }

            d.children.push({id: randomString(5), name: result.name, root: false, "entityTypeName": result.type, "entityType" : {
                "id" : 24,
                "name" : "CUSTOM_GROUP",
                "description" : "com.groundwork.collage.model.impl.CustomGroup",
                "isLogicalEntity" : true,
                "applicationTypeSupported" : false
                },
            children: [], childNames: [], parents: [d]});

            var root_ = getRootFromNode(d),
                args = rootNodes[root_.name];

            update(args[0], args[1], args[2], args[3]);
            */
            refresh();
        },
        function error() {});
    },
    function () {
    });
}

function editLeaf(d)
{
    var entityType = (d.hasEntity || d.entityTypeName),
        own = [], ownCopy = [], ownNames = [];

    var children = (d.children && d.children.length) ? d.children : ((d.children_ && d.children_.length) ? d.children_ : []);

    if(children && children.length)
    {
        for(var i = 0, iLimit = children.length; i < iLimit; i++)
        {
            var category = children[i];

            if(d.hasEntity)
            {
                for(var j = 0, jLimit = ((entityType == "HOSTGROUP") ? $scope.hostGroups.length : $scope.serviceGroups.length); j < jLimit; j++)
                {
                    var root_ = (entityType == "HOSTGROUP") ? $scope.hostGroups[j] : $scope.serviceGroups[j];

                    if(root_.name == category.name)
                    {
                        own.push(root_);
                        ownCopy.push(root_);
                        break;
                    }
                }
            }
            else
            {
                var root_ = category;

                if(root_.entityTypeName == "CUSTOM_GROUP")
                {
                    root_ = findNodeInHierarchy(root_.name);
                    own.push(root_);
                    ownCopy.push(root_);
                }
            }
        }
    }

    var existing = [];
    var treeType = getTreeEntityTypeName(d);

    var currentNode = findNodeInHierarchy(d.name);
    if (!!currentNode) {
        var cgParents = findParentsByName(currentNode);
        var cgChildren = findCustomGroupChildren(currentNode);
        _.forOwn($scope.hierarchies, function (hRoot, hRootKey) {
            buildExistingNodes(hRoot, d.name, existing, cgChildren, cgParents);
        });
    }

    if(treeType != "SERVICE_GROUP")
    {
        existing = existing.concat(_.reject($scope.hostGroups, function(hostGroup) {
            return _.contains(own, hostGroup);
        }));
    }

    if(treeType != "HOSTGROUP")
    {
        existing = existing.concat(_.reject($scope.serviceGroups, function(serviceGroup) {
            return _.contains(own, serviceGroup);
        }));
    }

    DataService.getCategory(d.name, d.entityTypeName).then(function(custom)
    {
        var modalInstance = $modal.open({
            templateUrl: '/portal-groundwork-base/app/views/modals/category-detail.html',
            controller: CategoryDetailsInstanceController,
            backdrop: false,
            resolve: {
                DataService: function() { return DataService; },
                category: function() { return {
                    name: d.name,
                    type: entityType,
                    description: custom.description,
                    id: d.id,
                    entities: own,
                    existingEntities: existing,
                    treeType: treeType,
                    isLeaf: (d.entityTypeName == "CUSTOM_GROUP"),
                    limitType: treeType
                } }
            }
        });

        modalInstance.result.then(function (result)
        {
            DataService.updateCategory({
                id: d.id,
                name: d.name,
                description: result.description,
                entityTypeName: d.entityTypeName
            })
                .then(function()
                {
                    var removals = $(ownCopy).not(result.entities).get(),
                        removalsType = removals.length && removals[0].hosts ? "HOSTGROUP" : (removals.length && removals[0].services ? "SERVICE_GROUP" : "CUSTOM_GROUP"),
                        additions = $(result.entities).not(ownCopy).get(),
                        additionsType = additions.length && additions[0].hosts ? "HOSTGROUP" : (additions.length && additions[0].services ? "SERVICE_GROUP" : "CUSTOM_GROUP");

                    var removeCategory = function(index, callback)
                    {
                        DataService.rootCategory(removals[index], true)
                            .then(function()
                            {
                                if((index + 1) == removals.length)
                                {
                                    callback();
                                }
                                else
                                {
                                    removeCategory(index + 1, callback);
                                }
                            });
                    };

                    var addAsChildCategory = function(index, callback)
                    {
                        DataService.addCategory(additions[index], d, true)
                            .then(function()
                            {
                                if((index + 1) == additions.length)
                                {
                                    callback();
                                }
                                else
                                {
                                    addAsChildCategory(index + 1, callback);
                                }
                            });
                    };

                    function handleRemovals(callback)
                    {
                        if(removals && removals.length)
                        {
                            if(removalsType != "CUSTOM_GROUP")
                            {
                                if(removals && removals.length)
                                {
                                    for(var i = 0, iLimit = removals.length; i < iLimit; i++)
                                    {
                                        removals[i] = (removals[i].objectId || removals[i].id);
                                    }
                                }

                                DataService.deleteCategoryEntities(d, removals, removalsType)
                                    .then(function()
                                    {
                                        callback();
                                    });
                            }
                            else
                            {
                                removeCategory(0, function()
                                {
                                    callback();
                                });
                            }
                        }
                        else callback();
                    }

                    function handleAdditions(callback)
                    {
                        if(additions && additions.length)
                        {
                            if(additionsType != "CUSTOM_GROUP")
                            {
                                if(additions && additions.length)
                                {
                                    for(i = 0, iLimit = additions.length; i < iLimit; i++)
                                    {
                                        additions[i] = additions[i].id;
                                    }
                                }

                                DataService.addCategoryEntities(d, additions, additionsType)
                                    .then(function()
                                    {
                                        callback();
                                    });
                            }
                            else
                            {
                                addAsChildCategory(0, function()
                                {
                                    callback();
                                });
                            }
                        }
                        else callback();
                    }

                    handleRemovals(function()
                    {
                        handleAdditions(function()
                        {
                            refresh();
                        });
                    });
                });
        }, function ()
        {
        });
    });
}

// NODE Hierarchy Helpers

// find Categories that are not assigned to this node and not a parent of this node and not an immediate child
function buildExistingNodes(node, name, existing, cgChildren, cgParents) {
    if (node.entityTypeName === 'CUSTOM_GROUP') {
        if ((node.name !== name) && !~cgChildren.indexOf(node.name) && !~cgParents.indexOf(node.name)) {
            existing.push(node);
        }
        if (!!node.children) {
            for (var ix = 0; ix < node.children.length; ix++) {
                buildExistingNodes(node.children[ix], name, existing, cgChildren, cgParents);
            }
        }
    }
}

// Note this function does not work correctly since some parent arrays are not filled out from server side
// Currently have to use findParentsByName
function findParents(currentNode) {
    var foundParents = [];
    function walkParents(node) {
        if (node.parents) {
            for (var ix = 0; ix < node.parents.length; ix++) {
                foundParents.push(node.parents[ix].name);
                walkParents(node.parents[ix]);
            }
        }
        return foundParents;
    }
    walkParents(currentNode);
    return foundParents;
}

function findParentsByName(currentNode) {
    var foundParents = [];
    function walkParents(n) {
        if (n.parentNames) {
            for (var ix = 0; ix < n.parentNames.length; ix++) {
                var x = findNodeInHierarchy(n.parentNames[ix]);
                foundParents.push(n.parentNames[ix]);
                walkParents(x);
            }
        }
        return foundParents;
    }
    walkParents(currentNode);
    return foundParents;
}

function findCustomGroupChildren(current) {
    var foundChildren = [];
    if (current.children) {
        for (var ix = 0; ix < current.children.length; ix++) {
            if (current.children[ix].entityTypeName === "CUSTOM_GROUP") {
                foundChildren.push(current.children[ix].name);
            }

        }
    }
    return foundChildren;
}

function findNodeInHierarchy(nodeName) {
    var foundNode = null;
    function walkInnerNode(node) {
        if (node.children) {
            for (var ix = 0; ix < node.children.length; ix++) {
                var child = node.children[ix];
                if (child.name === nodeName) {
                    foundNode = child;
                    return child;
                }
                walkInnerNode(child);
            }
        }
        return null;
    }
    _.forOwn($scope.hierarchies, function(hRoot, hRootKey) {
        if (nodeName === hRootKey) {
            foundNode = hRoot;
            return false;
        }
        walkInnerNode(hRoot);
        if (foundNode != null) {
            return false;
        }
    });
    return foundNode;
}

function deleteLeaf(d)
{
    if(!d.children || !d.children.length) {
        DataService.deleteLeafCategory(d)
            .then(function success()
            {
                var parent = d.parent;

                if(parent && parent.children)
                {
                    for(var i = 0; i < parent.children.length; i++)
                    {
                        if(parent.children[i].name == d.name)
                        {
                            parent.children.splice(i--, 1);
                            break;
                        }
                    }
                }

                var root_ = getRootFromNode(d),
                    args = rootNodes[root_.name];

                update(args[0], args[1], args[2], args[3]);
                
                refresh();
            },
            function error()
            {
            });
    }
    else
    {
        DataService.cascadeDeleteCategoryHierarchy(d, true)
            .then(function success()
            {
                var deleteChildren = function(parent)
                {
                    if(parent && parent.children)
                    {
                        for(var i = 0; i < parent.children.length; i++)
                        {
                            var child = parent.children[i];

                            if(child.children && child.children.length)
                            {
                                deleteChildren(child);
                            }

                            parent.children.splice(i--, 1);
                        }
                    }
                };

                deleteChildren(d);

                //

                var parent = d.parent;

                if(parent && parent.children)
                {
                    for(var i = 0; i < parent.children.length; i++)
                    {
                        if(parent.children[i].name == d.name)
                        {
                            parent.children.splice(i--, 1);
                            break;
                        }
                    }
                }

                var root_ = getRootFromNode(d),
                    args = rootNodes[root_.name];

                update(args[0], args[1], args[2], args[3]);

                refresh();
            },
            function error()
            {
            });
    }
}

function deleteCategory(d)
{
        DataService.deleteCategory(d, true)
            .then(function success()
            {/*
                var children = [];

                if(d.children)
                {
                    for(var i = 0; i < d.children.length; i++)
                    {
                        var child = d.children[i];
                    
                        children.push({name: child.name, entityTypeName: child.entityTypeName});
                    }
                }

                var deleteChildren = function(parent)
                {
                    if(parent && parent.children)
                    {
                        for(var i = 0; i < parent.children.length; i++)
                        {
                            var child = parent.children[i];

                            if(child.children && child.children.length)
                            {
                                deleteChildren(child);
                            }

                            parent.children.splice(i--, 1);
                        }
                    }
                };

                deleteChildren(d);

                //

                var parent = d.parent;

                if(parent && parent.children)
                {
                    for(var i = 0; i < parent.children.length; i++)
                    {
                        if(parent.children[i].name == d.name)
                        {
                            parent.children.splice(i--, 1);
                            break;
                        }
                    }
                }
*/
                //var root_ = getRootFromNode(d),
                //    args = rootNodes[root_.name];

                //update(args[0], args[1], args[2], args[3]);

                refresh();
            },
            function error()
            {
            });
}

function deleteCategoryOnly(d)
{
        DataService.deleteCategory(d, false)
            .then(function success()
            {
                var parent = d.parent, children = d.children;

                if(parent && parent.children)
                {
                    for(var i = 0; i < parent.children.length; i++)
                    {
                        if(parent.children[i].name == d.name)
                        {
                            parent.children.splice(i--, 1);

                            parent.children = parent.children.concat(children);
                            break;
                        }
                    }
                }

                var root_ = getRootFromNode(d),
                    args = rootNodes[root_.name];

                update(args[0], args[1], args[2], args[3]);
                
                refresh();
            },
            function error()
            {
            });
}

function setAsRoot(d)
{
    DataService.rootCategory(d, true)
        .then(function success()
        {
            var parent = d.parent;

            if(parent)
            {
                for(var i = 0; i < parent.children.length; i++)
                {
                    if(parent.children[i].name == d.name)
                    {
                        parent.children.splice(i--, 1);
                        break;
                    }
                }
            }

            var root_ = getRootFromNode(d),
                args = rootNodes[root_.name];

            update(args[0], args[1], args[2], args[3]);

            refresh();
        },
        function error()
        {
        });
}

});

var CategoryDetailsInstanceController = function ($scope, $modalInstance, DataService, category) {

    $scope.category = category || {
        name: '',
        description: '',
        entities: [],
        existingEntities: [],
        treeType: 'CUSTOM_GROUP'
    };

    $scope.filter = {
        name: ''
    };

    $scope.alerts = [];

    $scope.hideEntity = function(entity)
    {
        if ($scope.filter.name.length && entity.name.toLowerCase().indexOf($scope.filter.name.toLowerCase()) == -1) {
            return true;
        }

        // tree entity type of entity must be a custom group or match
        // the custom group's tree entity type
        var entityTreeEntityTypeName = getTreeEntityTypeName(entity);
        if (entityTreeEntityTypeName !== 'CUSTOM_GROUP') {
            var categoryTreeEntityTypeName = getTreeEntityTypeName($scope.category);
            if (categoryTreeEntityTypeName === 'CUSTOM_GROUP') {
                if (($scope.category.treeType !== 'CUSTOM_GROUP') && (entityTreeEntityTypeName !== $scope.category.treeType)) {
                    // entity tree entity type does not match initial tree entity type
                    return true;
                }
            } else if (entityTreeEntityTypeName !== categoryTreeEntityTypeName) {
                // entity tree entity type does not match category tree entity type
                return true;
            }
        }
        // types of custom group's entities must match
        if (!_.isEmpty($scope.category.entities) && (getEntityTypeName(entity) !== getEntityTypeName($scope.category.entities[0]))) {
            // entity type does not match category entities types
            return true;
        }

        return false;
    };

    $scope.addEntity = function(entity)
    {
        var index = _.indexOf($scope.category.existingEntities, entity);
        if (index != -1) {
            $scope.category.entities.push(entity);
            $scope.category.existingEntities.splice(index, 1);
        }
   };
    
    $scope.removeEntity = function(entity)
    {
        var index = _.indexOf($scope.category.entities, entity);
        if (index != -1) {
            $scope.category.existingEntities.push(entity);
            $scope.category.entities.splice(index, 1);
        }
    };

    $scope.add = function() {
        if (!$scope.category.name.match(/[0-9]/) &&
            ($scope.category.name.toLowerCase() == $scope.category.name.toUpperCase())) {
            errorMessage('Custom Group name does not contain an alphanumeric character. Please choose another name.');
            return;
        }
        DataService.getCategory($scope.category.name, "CUSTOM_GROUP").then(
            function success(entity) {
                errorMessage('Custom Group Name already exists: ' + entity.name + ". Please choose another name.");
            },
            function error(message) {
                if (message.indexOf('was not found') != -1) {
                    $modalInstance.close($scope.category);
                } else {
                    errorMessage('There was an error with communicating with the server. Please try again later.');
                }
            });
    };

    $scope.update = function() {
        $modalInstance.close($scope.category);
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };

    $scope.addFailureAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'danger', msg: message});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    function getEntityTypeName(entity) {
        return (entity.entityTypeName || (entity.hosts ? "HOSTGROUP" : (entity.services ? "SERVICE_GROUP" : "CUSTOM_GROUP")));
    }

    function getTreeEntityTypeName(entity) {
        var entityTypeName = getEntityTypeName(entity);
        if ((entityTypeName === 'CUSTOM_GROUP') && !!entity.entities) {
            _.forEach(entity.entities, function(entity) {
                entityTypeName = getTreeEntityTypeName(entity);
                if (entityTypeName !== 'CUSTOM_GROUP') {
                    return false;
                }
            });
        }
        if ((entityTypeName === 'CUSTOM_GROUP') && !!entity.children) {
            _.forEach(entity.children, function(child) {
                entityTypeName = getTreeEntityTypeName(child);
                if (entityTypeName !== 'CUSTOM_GROUP') {
                    return false;
                }
            });
        }
        return entityTypeName;
    }

    function errorMessage(message) {
        notyfy({text: message, type: 'error', timeout: 5000});
        console.log(message);
        $scope.addFailureAlert(message);
    }
};
CategoryDetailsInstanceController.$inject = ['$scope', '$modalInstance', 'DataService', 'category'];

var HostsManagementController = function ($scope, $modalInstance, $modal, DataService) {

    $scope.filter = { term: '' };

    $scope.alerts = [];

    $scope.gridOptions = {showSelectionCheckbox: true, selectedItems: [], data: 'rootsFiltered', columnDefs:
	[
		{field: 'name',        displayName: 'Host Group', width: '75%', cellTemplate: '<div ng-click="showHost(row.getProperty(\'name\'))"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
		{field: 'hostCount()', displayName: 'Host Count', width: '20%', cellTemplate: '<div ng-click="showHost(row.getProperty(\'name\'))"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'}
	],
    checkboxHeaderTemplate: '<div></div>',
    selectWithCheckboxOnly: true};

    $scope.showHost = function(name)
    {
        var host;

        for(var i = 0, iLimit = $scope.roots.length; i < iLimit; i++)
        {
            var host_ = $scope.roots[i];
            
            if(host_.name == name)
            {
                host = host_;
                break;
            }
        }

        if(!host)
            return;
    
        var modalInstance = $modal.open({
            templateUrl: '/portal-groundwork-base/app/views/modals/group-detail.html',
            controller: GroupDetailsInstanceController,
            backdrop: false,
            resolve: {
                DataService: function() { return DataService; },
                group: function() { return {
                    id: host.id,
                    name: host.name,
                    alias: host.alias,
                    description: host.description,
                    typeName: host.appTypeDisplayName,
                    hosts: host.hosts || []
                } }
            }
        });

        modalInstance.result.then(function (result)
        {
            DataService.createOrUpdateHostGroup({
                id: host.id,
                name: result.name,
                alias: result.alias,
                description: result.description,
                hosts: result.hosts
            })
            .then(function()
            {
                $scope.refresh();
            });
        },
        function () {
        });
    };

    $scope.refresh = function()
    {
        DataService.getHostGroups().then(
            function success(groups) {
                var roots = groups.hostGroups;

                var systemRoots = [];
                angular.forEach(roots, function (row)
                {
                    if (row.appType === 'SYSTEM') {
                        systemRoots.push(row);

                        row.hostCount = function () {
                            return row.hosts ? row.hosts.length : 0;
                        };
                    }
                });

                $scope.roots = systemRoots;

                $scope.doSearch();
            },
            function error(message) {
                console.log('>>>>>>>>>>>>>> get category hierarchy roots error: '+message);
            });
    };
    
    $scope.refresh();

    $scope.addHost = function()
    {
        var modalInstance = $modal.open({
            templateUrl: '/portal-groundwork-base/app/views/modals/group-detail.html',
            controller: GroupDetailsInstanceController,
            backdrop: false,
            resolve: {
                DataService: function() { return DataService; },
                group: function() { return {
                    name: '',
                    alias: '',
                    description: '',
                    typeName: 'SYSTEM',
                    hosts: []
                } }
            }
        });

        modalInstance.result.then(function (result)
        {
            DataService.createOrUpdateHostGroup({
                name: result.name,
                alias: result.alias,
                description: result.description,
                appType: 'SYSTEM',
                appTypeDisplayName: result.typeName,
                hosts: result.hosts
            }).then(function success()
            {
                $scope.refresh();
            },
            function error() {});
        },
        function () {
        });
    };

    $scope.deleteHosts = function()
    {
        var modalInstance = $modal.open({
            template: '<div class="modal-header"><button type="button" class="close" aria-hidden="true" ng-click="close()">&times;</button><h4 class="modal-title">Delete Host Groups</h4></div><form role="form" novalidate class="css-form"><div class="modal-body"><p class="text-danger">Are you sure you want to delete all the selected host groups?</p><p class="text-danger">This cannot be undone.</p></div><div class="modal-footer"><button class="btn btn-default" ng-click="close()">Cancel</button><button type="submit" class="btn btn-primary" ng-click="delete()">Delete</button></div></form>',
            controller: DeleteHostGroupsController
        });

        modalInstance.result.then(function ()
        {
            DataService.deleteHostGroups($scope.gridOptions.selectedItems).then(function()
            {
                $scope.refresh();
            },
            function failures(message, status)
            {
                $scope.addFailureAlert(message);
            });
        },
        function ()
        {
        });
    };

    $scope.doSearch = function()
    {
        clearTimeout(searchTimeout);
        searchTimeout = null;

        if(!$scope.filter.term.length)
        {
            $scope.rootsFiltered = angular.copy($scope.roots);
            return;
        }

        var filtered = [], term = $scope.filter.term.toLowerCase();

        angular.forEach($scope.roots, function (row)
        {
            if(row.name && (row.name.toLowerCase().indexOf(term) != -1))
            {
                filtered.push(row);
            }
        });

        $scope.rootsFiltered = filtered;
    };

    var searchTimeout = null;

    $scope.doSearchDelayed = function()
    {
        clearTimeout(searchTimeout);

        searchTimeout = setTimeout(function()
        {
            $scope.doSearch();
        },
        300);
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };

    $scope.addFailureAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'danger', msg: message});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };
};
HostsManagementController.$inject = ['$scope', '$modalInstance', '$modal', 'DataService'];

var GroupDetailsInstanceController = function ($scope, $modalInstance, DataService, group) {

    var SEARCH_LIMIT = 50;

    $scope.group = group ? angular.copy(group) : {
        id: '',
        name: '',
        alias: '',
        description: '',
        typeName: '',
        hosts: []
    };

    $scope.filter = { name: '' };

    $scope.hosts = [];

    var searchTimeout = null;

    $scope.search = function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function() {
            $scope.updateHosts();
        }, 300);
    };

    $scope.updateHosts = function() {
        // note: search limit increased to insure that after filtering for those
        // hosts already members of the group, at least the search limit remains
        var searchLimit = SEARCH_LIMIT+$scope.group.hosts.length;
        DataService.autocomplete($scope.filter.name, searchLimit, 'HOST').then(
            function success(names) {
                $scope.hosts = _.reject(names, function(name) {
                    return !!_.find($scope.group.hosts, {hostName: name.canonicalName});
                });
                $scope.hosts.length = Math.min(SEARCH_LIMIT, $scope.hosts.length)
            },
            function error(message) {
                console.log('autocomplete host names error: '+message);
            });
    };

    $scope.hideEntity = function(entity) {
        return $scope.filter.name.length && !~entity.hostName.toLowerCase().lastIndexOf($scope.filter.name.toLowerCase(), 0);
    };

    $scope.addHost = function(host) {
        var groupHost = {hostName: host.canonicalName};
        if (!_.find($scope.group.hosts, groupHost)) {
            $scope.group.hosts.push(groupHost);
        }
        $scope.updateHosts();
    };

    $scope.removeEntity = function(entity) {
        _.remove($scope.group.hosts, {hostName: entity.hostName});
        $scope.updateHosts();
    };

    $scope.addAllHosts = function() {
        _.forEach($scope.hosts, function(host) {
            var groupHost = {hostName: host.canonicalName};
            if (!_.find($scope.group.hosts, groupHost)) {
                $scope.group.hosts.push(groupHost);
            }
        });
        $scope.updateHosts();
    };

    $scope.alerts = [];

    $scope.clear = function() {
        $scope.filter.name = '';
        $scope.updateHosts();
    };

    $scope.add = function() {
        if (!$scope.group.name.match(/[0-9]/) &&
            ($scope.group.name.toLowerCase() == $scope.group.name.toUpperCase())) {
            errorMessage('Host Group name does not contain an alphanumeric character. Please choose another name.');
            return;
        }
        DataService.getHostGroup($scope.group.name).then(
            function success(entity) {
                errorMessage('Host Group Name already exists: ' + entity.name + ". Please choose another name.");
            },
            function error(message) {
                if (message.indexOf('was not found') != -1) {
                    group = angular.copy($scope.group);
                    $modalInstance.close(group);
                } else {
                    errorMessage('There was an error with communicating with the server. Please try again later.');
                }
            });
    };

    $scope.update = function() {
        group = angular.copy($scope.group);
        $modalInstance.close(group);
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };

    $scope.addFailureAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'danger', msg: message});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.updateHosts();

    function errorMessage(message) {
        notyfy({text: message, type: 'error', timeout: 5000});
        console.log(message);
        $scope.addFailureAlert(message);
    }
};
GroupDetailsInstanceController.$inject = ['$scope', '$modalInstance', 'DataService', 'group'];

var ServiceGroupsManagementController = function ($scope, $modalInstance, $modal, DataService)
{
    $scope.filter = { term: '' };

    $scope.alerts = [];

    $scope.gridOptions = {showSelectionCheckbox: true, selectedItems: [], data: 'rootsFiltered', columnDefs:
	[
		{field: 'name',        displayName: 'Service Group', width: '75%', cellTemplate: '<div ng-click="showHost(row.rowIndex)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
		{field: 'hostCount()', displayName: 'Service Count', width: '20%', cellTemplate: '<div ng-click="showHost(row.rowIndex)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'}
	],
    checkboxHeaderTemplate: '<div></div>',
    selectWithCheckboxOnly: true};

    $scope.showHost = function(index)
    {
        var host = $scope.roots[index];

        var modalInstance = $modal.open({
            templateUrl: '/portal-groundwork-base/app/views/modals/service-group-detail.html',
            controller: ServiceGroupDetailsInstanceController,
            backdrop: false,
            size: 'lg',
            resolve: {
                DataService: function() { return DataService; },
                group: function() { return {
                    id: host.id,
                    name: host.name,
                    description: host.description,
                    typeName: host.appTypeDisplayName,
                    services: host.services || []
                } }
            }
        });

        modalInstance.result.then(function (result)
        {
            DataService.createOrUpdateServiceGroup({
                id: host.id,
                name: result.name,
                description: result.description,
                services: result.services,
                appType: (!!result.appType) ? result.appType : null
            })
            .then(function()
            {
                $scope.refresh();
            });
        },
        function () {
        });
    };

    $scope.refresh = function()
    {
        DataService.getServiceGroups().then(
            function success(groups) {
                var roots = groups.serviceGroups;

                var systemRoots = [];
                angular.forEach(roots, function (row)
                {
                    if (row.appType === 'SYSTEM') {
                        systemRoots.push(row);

                        row.hostCount = function () {
                            return row.services ? row.services.length : 0;
                        };
                    }
                });

                $scope.roots = systemRoots;

                $scope.doSearch();
            },
            function error(message) {
                console.log('>>>>>>>>>>>>>> get category hierarchy roots error: '+message);
            });
    };
    
    $scope.refresh();

    $scope.addHost = function()
    {
        var modalInstance = $modal.open({
            templateUrl: '/portal-groundwork-base/app/views/modals/service-group-detail.html',
            controller: ServiceGroupDetailsInstanceController,
            backdrop: false,
            size: 'lg',
            resolve: {
                DataService: function() { return DataService; },
                group: function() { return {
                    name: '',
                    description: '',
                    typeName: 'SYSTEM',
                    services: []
                } }
            }
        });

        modalInstance.result.then(function (result) {
            DataService.createOrUpdateServiceGroup({
                name: result.name,
                description: result.description,
                services: result.services,
                appType: 'SYSTEM'
            }).then(function success()
            {
                $scope.refresh();
            },
            function error(e) {
                console.log("error retrieving service groups: " + e);
            });
        }, function () {
        });
    };

    $scope.deleteHosts = function()
    {
        var modalInstance = $modal.open({
            template: '<div class="modal-header"><button type="button" class="close" aria-hidden="true" ng-click="close()">&times;</button><h4 class="modal-title">Delete Host Groups</h4></div><form role="form" novalidate class="css-form"><div class="modal-body"><p class="text-danger">Are you sure you want to delete all the selected service groups?</p><p class="text-danger">This cannot be undone.</p></div><div class="modal-footer"><button class="btn btn-default" ng-click="close()">Cancel</button><button type="submit" class="btn btn-primary" ng-click="delete()">Delete</button></div></form>',
            controller: DeleteHostGroupsController
        });

        modalInstance.result.then(function ()
        {
            DataService.deleteServiceGroups($scope.gridOptions.selectedItems).then(function()
            {
                $scope.refresh();
            },
            function failures(message, status)
            {
                $scope.addFailureAlert(message);
            });
        },
        function ()
        {
        });
    };

    $scope.doSearch = function()
    {
        clearTimeout(searchTimeout);
        searchTimeout = null;

        if(!$scope.filter.term.length)
        {
            $scope.rootsFiltered = angular.copy($scope.roots);
            return;
        }

        var filtered = [], term = $scope.filter.term.toLowerCase();

        angular.forEach($scope.roots, function (row)
        {
            if(row.name && (row.name.toLowerCase().indexOf(term) != -1))
            {
                filtered.push(row);
            }
        });

        $scope.rootsFiltered = filtered;
    };

    var searchTimeout = null;

    $scope.doSearchDelayed = function()
    {
        clearTimeout(searchTimeout);

        searchTimeout = setTimeout(function()
        {
            $scope.doSearch();
        },
        300);
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };

    $scope.addFailureAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'danger', msg: message});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };
};
ServiceGroupsManagementController.$inject = ['$scope', '$modalInstance', '$modal', 'DataService'];

var ServiceGroupDetailsInstanceController = function ($scope, $modalInstance, DataService, group)
{
    var SEARCH_LIMIT = 50;

    $scope.group = group ? angular.copy(group) : {
        id: '',
        name: '',
        description: '',
        typeName: '',
        services: {}
    };

    $scope.filter = { host: '', service: '' };

    $scope.selectedHost = null;
    $scope.selectedCanonicalHost = null;

    $scope.selectedService = null;

    $scope.hosts = [];

    $scope.services = [];

    var searchTimeout = null;

    $scope.searchHosts = function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function() {
            if (!!$scope.selectedHost) {
                $scope.selectedHost = null;
                $scope.selectedCanonicalHost = null;
                $scope.updateServices();
            }
            $scope.updateHosts();
        }, 300);
    };

    $scope.updateHosts = function() {
        if (!!$scope.selectedService) {
            DataService.serviceHostNames($scope.selectedService).then(
                function success(names) {
                    $scope.hosts = _.filter(names, function (host) {
                        return !$scope.filter.host || ~host.name.toLowerCase().lastIndexOf($scope.filter.host.toLowerCase(), 0);
                    });
                    $scope.hosts = _.reject($scope.hosts, function(host) {
                        return !!_.find($scope.group.services, {hostName: host.canonicalName, description: $scope.selectedService});
                    });
                    $scope.hosts.length = Math.min(SEARCH_LIMIT, $scope.hosts.length)
                },
                function error(message) {
                    console.log('serviceHostNames host names error: ' + message);
                });
        } else {
            DataService.autocomplete($scope.filter.host, SEARCH_LIMIT, 'HOST').then(
                function success(names) {
                    $scope.hosts = names;
                },
                function error(message) {
                    console.log('autocomplete host names error: ' + message);
                });
        }
    };

    $scope.searchServices = function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function() {
            if (!!$scope.selectedService) {
                $scope.selectedService = null;
                $scope.updateHosts();
            }
            $scope.updateServices();
        }, 300);
    };

    $scope.updateServices = function() {
        if (!!$scope.selectedHost) {
            DataService.hostServiceDescriptions($scope.selectedCanonicalHost).then(
                function success(names) {
                    $scope.services = _.filter(names, function(service) {
                        return !$scope.filter.service || ~service.toLowerCase().lastIndexOf($scope.filter.service.toLowerCase(), 0);
                    });
                    $scope.services = _.reject($scope.services, function(service) {
                        return !!_.find($scope.group.services, {hostName: $scope.selectedCanonicalHost, description: service});
                    });
                    $scope.services.length = Math.min(SEARCH_LIMIT, $scope.services.length)
                },
                function error(message) {
                    console.log('hostServiceDescriptions service descriptions error: '+message);
                });
        } else {
            DataService.autocomplete($scope.filter.service, SEARCH_LIMIT, 'SERVICE').then(
                function success(names) {
                    $scope.services = _.pluck(names, 'name');
                },
                function error(message) {
                    console.log('autocomplete service descriptions error: '+message);
                });
        }
    };

    $scope.selectHost = function(host) {
        if ($scope.selectedHost === host.name) {
            $scope.selectedHost = null;
            $scope.selectedCanonicalHost = null;
            $scope.updateServices();
        } else if (!!$scope.selectedService) {
            var groupService = {hostName: host.canonicalName, description: $scope.selectedService};
            if (!_.find($scope.group.services, groupService)) {
                $scope.group.services.push(groupService);
            }
            $scope.updateHosts();
        } else {
            $scope.selectedHost = host.name;
            $scope.selectedCanonicalHost = host.canonicalName;
            $scope.updateServices();
        }
    };

    $scope.selectService = function(service) {
        if ($scope.selectedService === service) {
            $scope.selectedService = null;
            $scope.updateHosts();
        } else if (!!$scope.selectedHost) {
            var groupService = {hostName: $scope.selectedCanonicalHost, description: service};
            if (!_.find($scope.group.services, groupService)) {
                $scope.group.services.push(groupService);
            }
            $scope.updateServices();
        } else {
            $scope.selectedService = service;
            $scope.updateHosts();
        }
    };

    $scope.hideEntity = function(entity) {
        // note: cannot hide based on host filter since it operates on host names
        // and only canonical names can be tested against services
        if (!!$scope.selectedHost && (entity.hostName !== $scope.selectedCanonicalHost)) {
            return true;
        }
        if ((!!$scope.filter.service && !~entity.description.toLowerCase().lastIndexOf($scope.filter.service.toLowerCase(), 0)) ||
            (!!$scope.selectedService && (entity.description !== $scope.selectedService))) {
            return true;
        }
        return false;
    };

    $scope.removeEntity = function(entity) {
        _.remove($scope.group.services, {hostName: entity.hostName, description: entity.description});
        if (!!$scope.selectedHost) {
            $scope.updateServices();
        }
        if (!!$scope.selectedService) {
            $scope.updateHosts();
        }
    };

    $scope.addAllHosts = function() {
        if (!!$scope.selectedService) {
            _.forEach($scope.hosts, function(host) {
                var groupService = {hostName: host.canonicalName, description: $scope.selectedService};
                if (!_.find($scope.group.services, groupService)) {
                    $scope.group.services.push(groupService);
                }
            });
            $scope.updateHosts();
        }
    };

    $scope.addAllServices = function() {
        if (!!$scope.selectedHost) {
            _.forEach($scope.services, function(service) {
                var groupService = {hostName: $scope.selectedCanonicalHost, description: service};
                if (!_.find($scope.group.services, groupService)) {
                    $scope.group.services.push(groupService);
                }
            });
            $scope.updateServices();
        }
    };

    $scope.alerts = [];

    $scope.clear = function() {
        $scope.filter.host = '';
        $scope.filter.service = '';
        $scope.selectedHost = null;
        $scope.selectedCanonicalHost = null;
        $scope.selectedService = null;
        $scope.updateHosts();
        $scope.updateServices();
    };

    $scope.add = function() {
        if (!$scope.group.name.match(/[0-9]/) &&
            ($scope.group.name.toLowerCase() == $scope.group.name.toUpperCase())) {
            errorMessage('Service Group name does not contain an alphanumeric character. Please choose another name.');
            return;
        }
        DataService.getServiceGroup($scope.group.name).then(
            function success(entity) {
                errorMessage('Service Group Name already exists: ' + entity.name + ". Please choose another name.");
            },
            function error(message) {
                if (message.indexOf('was not found') != -1) {
                    group = angular.copy($scope.group);
                    $modalInstance.close(group);
                } else {
                    errorMessage('There was an error with communicating with the server. Please try again later.');
                }
        });
    };

    $scope.update = function() {
        group = angular.copy($scope.group);
        $modalInstance.close(group);
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };

    $scope.addFailureAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'danger', msg: message});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.updateHosts();
    $scope.updateServices();

    function errorMessage(message) {
        notyfy({text: message, type: 'error', timeout: 5000});
        console.log(message);
        $scope.addFailureAlert(message);
    }
};
ServiceGroupDetailsInstanceController.$inject = ['$scope', '$modalInstance', 'DataService', 'group'];

var DeleteHostGroupsController = function ($scope, $modalInstance) {

    $scope.delete = function() {
        $modalInstance.close();
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };
};
DeleteHostGroupsController.$inject = ['$scope', '$modalInstance'];
