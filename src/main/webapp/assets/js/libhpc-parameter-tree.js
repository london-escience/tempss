/**
 * Plugin for the Libhpc parameter tree.
 *
 * Contains utility functions:
 *     markValidity - for marking validity of ul nodes
 *     validateNodeAndParents - for validating this node
 *                              and all nodes above in tree
 *     validateParentNodes - for validating all ul nodes
 *                           above this one in tree
 */
;(function( $ ) {
    $.fn.extend({
        /**
         * Marks the validity of a ul node with a class.
         *
         * The class is 'valid', 'invalid', or neither.
         * A 'nodeValid' or 'nodeInvalid' event is fired too.
         * NOTE: The event is fired using "triggerHandler" which means only
         * handlers attached to the ul node itself will see the event - it
         * will NOT propagate up the tree.
         * If the node is invalid then a tooltip is displayed
         * with the contents of options.message.
         *
         * @param validity - one of 'none', 'valid' or 'invalid'.
         * @param options - options object
         *                  options.message contains message to
         *                  display in invalid tooltip.
         */
        markValidity: function(validity, options) {
            var defaults = {
                    message: null
            };
            options = $.extend({}, defaults, options);

            return this.each( function() {
                var $ul = $(this).closest('ul');

                switch(validity) {
                    case 'none':
                        // Remove all related classes
                        $ul.removeClass('invalid').removeClass('valid');
                        // TODO: Check if we need to fire the nodeInvalid event here
                        $ul.triggerHandler('nodeInvalid', $ul);
                        // Remove the tool-tip icon (only for this node)
                        $ul.children().first().children('.val-help').remove();
                        break;
                    case 'valid':
                        // Add the valid class to display the valid symbol
                        $ul.removeClass('invalid').addClass('valid').triggerHandler('nodeValid');
                        // Remove the tool-tip icon
                        $ul.find('.val-help').remove();
                        break;
                    case 'invalid':
                        // Add the invalid class to display a red X for this node
                        $ul.removeClass('valid').addClass('invalid').triggerHandler('nodeInvalid', $ul);
                        // Add the tool-tip with the message.
                        if (options.message !== undefined) {
                            $ul.children().first().append('<i class="glyphicon glyphicon-question-sign val-help" ' +
                                    'style="padding-left: 10px;" ' +
                                    'title="' + options.message + '" ' +
                                    'data-toggle="tooltip" data-placement="right"></i>');
                        }
                        break;
                }
            });

        },

        /**
         * Check the validity of a ul node (or the owning ul of any other
         * type of element).
         *
         * A node is valid if all ul's which are children of this elements
         * li's are either valid, not chosen, or disabled.
         */
        validateNodeAndParents: function () {
            return this.each(function() {
                // Only 'ul' is defined as valid or not.
                // Get the owning ul.
                // closest() traverses up the tree (including the element itself)
                var $owningUL = $(this).closest('ul');

                if ($owningUL.length > 0) {
                    if ($owningUL.data('disabled') === true) {
                        // Node is not valid if it is disabled
                        $owningUL.markValidity('none');
                    } else if ($owningUL.data('leaf') === true) {
                        // Node is valid if it has no inputs, else
                        // depends on validity of input, as determined
                        // by presence of class.
                        if (!($owningUL.hasInput())) {
                            $owningUL.markValidity('valid');
                        } else {
                            if ($owningUL.hasClass('valid')) {
                                $owningUL.markValidity('valid');
                            } else if ($owningUL.hasClass('invalid')) {
                                $owningUL.markValidity('invalid');
                            } else {
                                $owningUL.markValidity('none');
                            }
                        }
                    } else {
                        var isValid = true;
                        // If node has no input items and is not a leaf is not valid
                        // (eg First item of select dropdown)
                        if ($owningUL.children('li').length === 0) {
                            isValid = false;
                        } else {
                            // Node is valid if there are no ul children of it's li which
                            // are not marked as valid or are not marked as chosen.
                            // (.each loop will not run in that case).
                        	// Feb 17: This selector fails if a child UL is valid
                        	// but has multiple classes on it. Fixed to resolve this.
                            //$owningUL.children('li').children('ul:not([class="valid"], [chosen="false"])').each(function(i, childUL) {
                        	$owningUL.children('li').children('ul').not('.valid, [chosen="false"]').each(function(i, childUL) {
                                // If any remaining ul's are not disabled then is not valid.
                                // TODO: Convert this into check on class="disabled" for performance?
                                if ($(childUL).data('disabled') !== true) {
                                    isValid = false;
                                    // Break out of .each loop
                                    return false;
                                }
                            });
                        }
                        // Mark node as valid
                        if (isValid) {
                            $owningUL.markValidity('valid');
                        } else {
                            $owningUL.markValidity('none');
                        }
                    }

                    // Check parent nodes up the tree
                    $owningUL.validateParentNodes();
                }
            });
        },

        /**
         * Mark the validity of the ascending parent ul nodes of the ul owning
         * this node in the tree.
         * Note that the ul owning this node is not checked, as the usage pattern
         * is that the validity has been set.
         *
         * If all the parent's ul children are valid, then mark as valid, else
         * mark as none.
         */
        validateParentNodes: function() {
            return this.each(function() {
                // Only 'ul' is defined as valid or not.
                // Start from the owning ul.
                // closest() traverses up the tree (including element itself)
                var $owningUL = $(this).closest('ul');

                // Now get parent ul.
                var $parentUL = $owningUL.parent().closest('ul');

                $parentUL.validateNodeAndParents();

            });
        },

        /**
         * Get the deepest ul of an element.
         */
        getULsByDepth: function() {
//            return this.each(function() {
                //var uls = $(this).find('ul');
                var ulsLengthMap = this.map(function() {
                    return {length: $(this).parents().length, element: this};
                });

                ulsLengthMap.sort(function(a,b) {
                    return a.length - b.length;
                });

                var i = ulsLengthMap.length;
                var result = [];
                while (i--) {
                    result.push(ulsLengthMap[i].element);
                }
                return $(result);
//            });
        },

        /**
         * Check if an element has any inputs below it in the tree.
         */
        hasInput: function() {
            var isTextInput = ($(this).find('input').length > 0);
            var isDropDown = ($(this).find('select').length > 0);
            return (isTextInput || isDropDown);
        },
    });

})( jQuery );

/**
 * Validate whether a given variable is an integer or not.
 *
 * @param valueToCheck variable to check.
 * @return boolean
 */
function isInteger(valueToCheck) {
    if (isNaN(valueToCheck)) {
        return false;
    }
    if (isNaN(parseInt(valueToCheck, 10))) {
        return false;
    }
    return (parseInt(Number(valueToCheck)) == valueToCheck);
}

/*!
 * jQuery plugin for libhpc parameter trees.
 */
;(function ($, window, document, undefined) {
    // Window and document are passed as local rather than global
    // for quicker resolution and so can be more easily minified.

    // undefined is used here as the undefined global
    // variable in ECMAScript 3 and is mutable (i.e. it can
    // be changed by someone else). undefined isn't really
    // being passed in so we can ensure that its value is
    // truly undefined. In ES5, undefined can no longer be
    // modified.

    // Create the defaults
    var pluginName = "LibhpcParameterTree",
    defaults = {
            host: "localhost",
            port: null,
            tempssBase: "tempss-service",
            xmlIndentation: "    ",
            collapseTree: true,
            disableOptionalBranches: true,
            enableTooltips: true
    };

    // The plugin constructor
    function LibhpcParameterTree(element, options) {
        // The DOM node which called this plugin
        this._element = element;
        this._$element = $(element);

        this._options = $.extend({}, defaults, options);
        this._defaults = defaults;
        this._name = pluginName;

        this.init();
    }

    LibhpcParameterTree.prototype = {
            /**
             * Initialisation.
             */
            init: function() {
                // Get the root tree ul and cache this for use elsewhere
                this._tree = this._$element.find($('ul[role="tree"]'));
                // Cache data leaf uls
                this._dataLeaves = this._tree.find('ul[data-leaf="true"]');
                // Cache optional uls
                this._optionalULs = this._tree.find('ul[data-optional="true"]');
                this._mandatoryULs = this._tree.find('ul[data-optional="false"]');
                this._optionalLIs = this._optionalULs.children('li.parent_li');
                // Cache all lis
                //this._allLIs = this._tree.find('li');
                this.updateLIs();
                // Cache uls which are choices
                this._choiceULs = this._tree.find('ul[choice-id]');
                // Cache repeatable uls and lis
                this._repeatableULs = this._tree.find('ul[data-max-occurs]').filter(function() {
                    return ($(this).attr('data-max-occurs') !== '1');
                });
                this._repeatableLIs = this._repeatableULs.children('li.parent_li');

                // Collapse this initial tree
                if (this._options.collapseTree) {
                    this.collapseTree();
                }

                // Disable optional branches
                if (this._options.disableOptionalBranches) {
                    this.disableOptionalBranches();
                }

                // Set any leaves without inputs to be valid immediately
                this.setLeavesWithoutInputsToValid();

                // Attach click handlers for dealing with expandable branches
                this.attachExpandClickHandlers();

                // Attach click handlers for dealing with optional branches
                this.attachRepeatClickHandlers();

                // Enable bootstrap tooltips
                if (this._options.enableTooltips) {
                    this.enableTooltips();
                }
            },

            /**
             * Destruction.
             */
            destroy: function() {
                this._$element.removeData();
            },

            /**
             * Sets any leaves which don't have inputs to be valid.
             * This is when the leaf shows the results of a choice.
             * This should happen on tree creation.
             */
            setLeavesWithoutInputsToValid: function() {
                this._dataLeaves.each(function () {
                    if (!($(this).hasInput())) {
                        $(this).markValidity('valid');
                    }
                });
            },

            /**
             * Hide all the nodes of the tree except the root node.
             */
            collapseTree: function() {

                // Hide all li nodes
                this._allLIs.hide();

                // Hide all ul nodes that are choices
                // (i.e. have choice-id attribute).
                // This ensures when we click on a choice bubble which has
                // been selected, nothing will show.
                // This is a trick. We are using the fact that the tree
                // javascript only acts on li nodes.
                this._choiceULs.hide();

                // Show the root element of the tree (li child of tree ul).
                var topLi = this._tree.children('li');
                topLi.show();
                // Show the top level li elements of the tree.
                topLi.children('ul').children('li').show();
            },

            /**
             * Expand the tree.
             */
            expandTree: function() {
                // Expand all li elements
                this._allLIs.show();
            },

            /**
             * Hide all optional branches.
             */
            hideDisabledBranches: function() {
                var disabledLIs = this._optionalULs.filter('.disabled').children('li.parent_li');
                disabledLIs.hide();
            },

            /**
             * Show all optional branches.
             */
            showDisabledBranches: function() {
                var disabledLIs = this._allLIs.filter(':visible').children('ul[data-optional="true"]').children('li.parent_li');
                disabledLIs.show();
            },

            /**
             * Attach the click handler to expand or collapse branches.
             */
            attachExpandClickHandlers: function() {
                // Rather than adding many click handlers for each span
                // element, add a single handler to the tree.
                // This is more efficient and can cope with additions
                // to the tree.
                this._tree.on('click' + '.' + this._name, 'li.parent_li > span.badge', function(e) {
                    treeExpandClickHandler(e, this);
                });
            },

            /**
             * Attach the click handlers for repeatable branches, both
             * addition and removal.
             */
            attachRepeatClickHandlers: function() {
                // Repeat button handler
                this._tree.on('click' + '.' + this._name, 'li.parent_li > span.repeat_button_add', function(e) {
                    repeatBranch($(this).closest('ul'));
                });
                // Remove button handler
                this._tree.on('click' + '.' + this._name, 'li.parent_li > span.repeat_button_remove', function(e) {
                    removeBranch($(this).closest('ul'));
                });
                // Repeat button HTML
                // This is done in XML transform now
                // this._repeatableLIs.children('span.badge').after('&nbsp;<span class="repeat-button glyphicon glyphicon-plus" title="This branch is repeatable - click to add another" aria-hidden="true"></span>&nbsp;');
            },

            /**
             * Enable tooltips using bootstrap.
             */
            enableTooltips: function() {
                $('body').tooltip({
                    selector: ".val-help, .tree-control-btn, .node-info"
                });
            },

            /**
             * Disable any optional branches.
             */
            disableOptionalBranches: function() {
                // Add click handler to switch on/off optional branches
                this._tree.on('click' + '.' + this._name, 'li.parent_li > span.toggle_button', function(e) {
                    toggleBranch($(this).closest('ul'));
                });
                // Add button to switch on optional branches
                //this._optionalLIs.children('span.badge').after('&nbsp;<span class="toggle-button enable-button glyphicon glyphicon-off" title="Optional branch disabled - click to activate" aria-hidden="true"></span>');
                setupOptionalBranches(this._optionalULs, this._optionalLIs);
                // Validate all non-optional branches
                this._mandatoryULs.getULsByDepth().validateNodeAndParents();
            },

            /**
             * Add handler for file change events
             */
            addFileReader: function() {
                this._tree.find('input[type="file"]').change(function () {
                    var $currentElement = $(this);
                    var fileInput = $currentElement[0];
                    var file = fileInput.files[0];
                    var textType = /text.*/;

                    if (file.type.match(textType)) {
                        var reader = new FileReader();
                        reader.onload = function (e) {
                            var fileText = reader.result;
                            // Set the file contents on the file upload object.
                            $currentElement.data("fileContents", fileText);
                        };
                        reader.readAsText(file);
                    } else {
                        // No other file types handled as yet
                    }
                });
            },

            /**
             * Get the xml representation of the tree
             * @returns {String}
             */
            getXmlProfile: function() {

                var rootNode = this._tree.children('li');
                var thisName = $.trim(rootNode.children('span').data('fqname'));

                var xmlString = '<?xml version="1.0" encoding="UTF-8"?>\n';
                xmlString += '<' + thisName + '>\n';
                xmlString += generateChildXML(rootNode, this._options.xmlIndentation, this._options.xmlIndentation, false);
                xmlString += '</' + thisName + '>\n';

                return xmlString;
            },

            /**
             * Load an XML profile into the tree.
             * @param xmlProfile String containing the XML profile.
             */
            loadXmlProfile: function(xmlProfile) {
                var rootNode = this._tree.children('li');
                var rootName = $.trim(rootNode.data('fqname'));
                var xmlDoc = $.parseXML(xmlProfile);
                var $xml = $(xmlDoc);
                var $xmlTreeNode = $(xmlDoc).children().first();
                loadXMLIntoTree($xmlTreeNode, $(rootNode));
            },

            /**
             * Convert a valid profile in the tree into a specific
             * input file.
             */
            convertProfile: function(successCallback, errorCallback) {
                var profileXml = getXMLProfile();
                _processProfile(profileXml, templateId);
            },
            
            /**
             * Update the list of LIs stored for this profile.
             */
            updateLIs: function() {
            	this._allLIs = this._tree.find('li');
            }
    };

    // Private functions    
    /**
     * Generate XML for a given node in the tree.
     * This operates recursively.
     *
     * @returns {String}
     */
    var generateChildXML = function(node, indentationString, depthString, useFileContent) {

        var xmlString = "";
        // For each child node, fill in the xml.

        $(node).children('ul:not([chosen="false"])').each(function(i, childUL) {
            // Ignore disabled nodes of tree
            if (!($(childUL).data('disabled'))) {
                var isLeaf = ($(childUL).data('leaf') === true);

                $(childUL).children('li').each(function(j, childLI) {
                    // Name is in span element
                    var nodeName = $.trim($(childLI).data('fqname'));
                    var inputValue = '';
                    var unitValue = '';
                    if (isLeaf) {
                        // If it's an input box, get the text
                        if ($(childLI).children('input:not([data-unit="true"])').length) {
                            inputValue = $.trim($(childLI).children('input:not([data-unit="true"])').val());
                        }
                        // If it's a dropdown select box, get the selection
                        if ($(childLI).children('select').length) {
                            inputValue = $(childLI).children('select').val();
                        }
                        // If it's an uploaded file, get the file contents or filename as appropriate
                        if ($(childLI).children('span').children('input[type="file"]').length) {
                            if (useFileContent) {
                                inputValue = $(childLI).children('span').children('input[type="file"]').data('fileContents');
                            } else {
                                var fakeFilePath = $(childLI).children('span').children('input[type="file"]').val();
                                // Use reg exp to remove the fake file path (everything before the file name)
                                var fileName = fakeFilePath.replace(/^.*\\/, "");
                                inputValue = fileName;
                            }
                        }
                        // Get the unit value if it exists
                        if ($(childLI).children('input[data-unit="true"]').length) {
                            unitValue = $.trim($(childLI).children('input[data-unit="true"]').val());
                        }
                    }

                    xmlString += depthString + "<" + nodeName
                    // Add the unit as an attribute
                    if (unitValue != '') {
                        xmlString += ' UNIT="' + unitValue + '"';
                    }
                    xmlString += ">";
                    xmlString += inputValue;
                    if (!isLeaf) {
                        xmlString += "\n" + generateChildXML(childLI, indentationString, depthString + indentationString, useFileContent) + depthString;
                    }
                    xmlString += "</" + nodeName + ">\n";

                });
            }
        });

        return xmlString;

    };

    /**
     * Expand or collapse branch. The element passed in is a span element
     * belonging to a li.
     */
    var treeExpandClickHandler = function(event, element) {
        // Only expand branches which are not disabled
        if ($(element).closest('ul').data('disabled') !== true) {
            var $element = $(element);
            var siblings = $element.siblings('ul');
            var children = siblings.children('li');
        	//var children = $(element).siblings('ul').children('li');
            if (children.is(':visible')) {
                //children.hide('fast');
                children.hide();
                // $(element).find(' > i').addClass('icon-plus-sign').removeClass('icon-minus-sign');
            } else {
                //children.show('fast');
            	// Testing show without animation due to multiple branch display issue
            	children.show();
                //$(element).find(' > i').addClass('icon-minus-sign').removeClass('icon-plus-sign');
            }
        }
        event.stopPropagation();
    };

    /**
     * Disable all optional branches.
     */
    var setupOptionalBranches = function(optionalULs, optionalLIs) {
        optionalULs.addClass('disabled').data('disabled', true);
        optionalLIs.children(':input').prop('disabled', true);
        optionalLIs.children('span').children('input').prop('disabled', true);
    };

    /**
     * Disable a branch.
     */
    var toggleBranch = function(elementUL) {
        if ($(elementUL).data('disabled') === true) {
            // Enable branch
            $(elementUL).removeClass('disabled')
                        .data('disabled', false)
                        .children('li.parent_li')
                        .validateNodeAndParents()
                        .each(function(i, elementLI) {
                $(elementLI).children('span.toggle_button')
                            .attr('title', 'Optional branch enabled - click to de-activate')
                            .children('i.toggle_button')
                            .removeClass('enable_button')
                            .addClass('disable_button')
                            .attr('title', 'Optional branch enabled - click to de-activate');

                // Enable all inputs
                $(elementLI).children(':input').prop('disabled', false).trigger('change');
                $(elementLI).children('span').children('input').prop('disabled', false).trigger('change');
                // Expand branch
                //$(elementLI).children('ul').children('li').show('fast');
                // Testing show without animation due to multiple branch display issue
                $(elementLI).children('ul').children('li').show();
            });
        } else {
            // Disable branch
            $(elementUL).addClass('disabled')
                        .data('disabled', true)
                        .children('li.parent_li')
                        .validateNodeAndParents()
                        .each(function(i, elementLI) {
                $(elementLI).children('span.toggle_button')
                            .attr('title', 'Optional branch disabled - click to activate')
                            .children('i.toggle_button')
                            .removeClass('disable_button')
                            .addClass('enable_button')
                            .attr('title', 'Optional branch disabled - click to activate');

                // Disable all inputs
                $(elementLI).children(':input').prop('disabled', true);
                $(elementLI).children('span').children('input').prop('disabled', true);
                // Hide branch
                //$(elementLI).children('ul').children('li').hide('fast');
                $(elementLI).children('ul').children('li').hide();
            });
        }
    };

    /**
     * Repeat this branch.
     */
    var repeatBranch = function(elementUL) {
        // Get the name of the item we're repeating
    	var $ul = $(elementUL);
    	var choice_path = $ul.find('select.choice').attr('choice-path');
    	
    	var $newElement = $ul.clone(true);
    	// Add an additional parameter to this node so we know which number
        // of a repeated element it is.
        var numElements = $ul.parent().find('select[choice-path="' + choice_path + '"]').length;
        $newElement.attr('data-repeat', numElements+1);
    	
        // Update Feb 17: when cloning a repeatable element, we clone the 
        // element and its descendants in their current state. This causes an
        // issue when loading a profile that has multiple instances of a 
        // repeatable element since we get stray elements left behind and 
        // unexpected results with multiple unwanted elements displaying. We 
        // now revert a cloned branch back to its original clean state before 
        // adding it into the tree.
        // First get the name of the element we're repeating
        var name = $newElement.children('li.parent_li').data('fqname');
        // Find any repeatable elements and ensure we have only 1 of each
        var addButtonItems = $newElement.find('span.repeat_button_add').parent(
        		'[data-fqname != ' + name + ']').toArray();
        while(addButtonItems.length > 0) {
        	var $item = $(addButtonItems.pop());
        	var $parentUL = $item.closest('ul');
        	var $repeatedElements = $parentUL.siblings('ul[data-repeat]').children('li.parent_li[data-fqname="' + $item.data('fqname') + '"]');
        	if($repeatedElements.length > 0) {
        		var $parentElements = $repeatedElements.parent();
            	$parentElements.remove();
            	// Now rebuild the addButtonItems list to take into account the
            	// removed items.
            	addButtonItems = $newElement.find('span.repeat_button_add').parent(
                		'[data-fqname != ' + name + ']').toArray();
        	}
        }
        
        /*
        $addButtonItems.each(function() {
        	// Ignore the top level add button for the element we're cloning
        	var currentName = $(this).parent().data('fqname');
        	if(currentName == name) return;
        	
        	// For the element with currentName, get the main ul and look for
        	// siblings of the same name which we can then remove.
        	var $parentUL = $(this).closest('ul');
        	var $repeatedElements = $parentUL.siblings('ul[data-repeat]').children('li.parent_li[data-fqname="' + currentName + '"]');
        	var $parentElements = $repeatedElements.parent();
        	$parentElements.remove();
        });
        */
        
        // Now clear any previously populated fields in newElement
    	// Now reset the fields in the repeated block
    	var $inputItems = $newElement.find('input[type="text"]');
    	$inputItems.each(function() { $(this).val(""); });
    	var $selectItems = $newElement.find('select option');
    	$selectItems.each(function() { $(this).prop('selected', false); });
    	var $validEl = $newElement.find('.valid');
    	$validEl.each(function() {
    		$(this).removeClass('valid');
    	});
        
    	// Copy this UL and insert into the tree directly after.
        if ($ul.children('li.parent_li').children('span.repeat_button_remove').length == 0) {
        	$newElement.insertAfter($ul).children('li.parent_li').children('span.badge').after('&nbsp;<span class="repeat_button repeat_button_remove" title="Click to remove this copy" aria-hidden="true"><i class="repeat_button repeat_button_remove"></i></span>&nbsp;');
        }
        else {
        	$newElement.insertAfter($ul);
        }
        
        // Find any on/off elements in the new block and switch them off so 
    	// that they validate as correct by default
    	var $toggleButtons = $newElement.children('li.parent_li').children('ul').find('i.toggle_button');
    	$toggleButtons.each(function() {
    		if($(this).hasClass('disable_button')) {
    			log("Disabling repeatable element in new block...");
    			$(this).trigger('click');
    		}
    	});
        
        $newElement.trigger('change');
    };

    /**
     * Remove this branch.
     */
    var removeBranch = function(elementUL) {
        $(elementUL).remove();
    };
    
    /**
     * Expand a branch - check to see if it is already expanded and if not, 
     * trigger a click to expand it
     */
    var expandBranch = function($elementLI) {
    	// If a node is optional and is disabled, don't bother making checks 
    	// to expand it
    	var $toggle = $elementLI.children('span.toggle_button');
    	if($toggle.length > 0 && $elementLI.closest('ul').data('disabled')) {
    		return;
    	}
    	
    	// The li.parent_li element of one of the node's descendants needs to 
    	// be tested for visiblity since the parent ul node will always report 
    	// as being visible.
    	var $nodes = $elementLI.find('> ul > li.parent_li');
    	if($nodes.length > 0) {
    		if($($nodes[0]).is(":hidden")) {
    			$elementLI.children('span.badge').trigger('click');
    		}
    	}
    };
    
    /**
     * Load a given XML tree into this HTML tree.
     *
     * @param $xmlTree jQuery xml object containing xml tree.
     */
    var loadXMLIntoTree = function($xmlElement, $parentHTMLElement) {
    	_loadXMLIntoTree($xmlElement, $parentHTMLElement);
    	// Update the list of LIs so that collapsing/expanding the tree 
    	// operates correctly
    	$templateContainer.data('plugin_LibhpcParameterTree').updateLIs();
    }

    var _loadXMLIntoTree = function($xmlElement, $parentHTMLElement) {
        $xmlElement.children().each(function() {
            var nodeName = $(this).prop('nodeName');
            // TODO - if disabled then activate, as must have been active
            // to get saved.
            var textValue = '';
            console.log('Processing node: ' + nodeName);
            var childNode = $(this)[0].childNodes[0];
            if (childNode) {
                if (childNode.nodeType == 3) {
                    textValue = childNode.nodeValue.trim();
                    console.log('Got text value: ' + textValue);

                }
            }
            var unit = this.getAttribute('UNIT');
            if (unit) {
                console.log('Got unit: ' + unit);
            }
            var elementLI = getTreeLiElement($parentHTMLElement, nodeName);
            var $elementLI = $(elementLI);
            var $owningUL = $($elementLI.parent('ul'));
            
            // If we're dealing with an element that is not already loaded, 
            // trigger a click on the node to expand it...
            // If the element already has a loaded flag then we'll be creating 
            // a duplicate and we trigger the click to expand that separately 
            // when the duplicate is created.
            if( ($owningUL.data('loaded') === undefined) || 
            	($owningUL.data('loaded') === false) ) {
            	expandBranch($elementLI);
            	//$elementLI.children('span.badge').trigger('click');
            }
            
            // Activate element if disabled
            if ($owningUL.data('disabled') === true) {
                console.log('Toggling branch');
                toggleBranch($owningUL[0]);
            }
            // Check to see if this has been populated already (for repeated elements).
            // If so need to create another.
            if ($owningUL.data('loaded') === true) {
                console.log('Branch already populated: ', $owningUL);
                if ($owningUL.data('max-occurs') != 1) {
                	//repeatBranch($owningUL[0]);
                	$elementLI.children('span.repeat_button_add').click();
                    
                    //$owningUL.find('*').removeData('loaded');
                    elementLI = getTreeLiElement($parentHTMLElement, nodeName);
                    $elementLI = $(elementLI);
                    
                    $owningUL = $($elementLI.parent('ul'));
                    //$owningUL.show('fast');
                    $owningUL.find('ul').removeData('loaded')
                    
                    // Trigger a click to expand the newly generated element
                    // if it has not been generated in an expanded state...
                    expandBranch($elementLI);
                    
                    // We now disable any optional elements in the new branch
                    // These will be enabled if data in a profile specifies 
                    // that they have data.
                    
                    
                    // This attempt to set the styles doesn't seem to be working correctly
                    // There is still a major issues with additional repeatable elements
                    // not expanding when clicked if they have a select box.
                    // Fix is applied below after the select.change()
                    $owningUL.find('li').removeAttr('style').attr('style', 'display: list-item;');
                    console.log('New UL: ', $owningUL[0]);
                }
            }
            var isLeaf = ($owningUL.data('leaf') === true);
            if (isLeaf) {
                if ($elementLI.children('input').length) {
                    // Set the entry and call the onchange function
                    $elementLI.children('input[data-unit!="true"]').val(textValue).change();
                    // Set the unit if one exists in XML
                    if (unit) {
                        $elementLI.children('input[data-unit="true"]').val(unit).change();
                    }
                }
                // If it's a dropdown select box, get the selection
                if ($elementLI.children('select').length) {
                    $elementLI.children('select').val(textValue).change();
                }
                // Might also be dropdown select for Default or Specified
                parentLI = $elementLI.parent('ul').parent('li');
                if ($(parentLI).children('select').length) {
                    $(parentLI).children('select').val(nodeName).change();
                }
            }
            // If it's a choice via dropdown select but not a leaf
            if (!isLeaf && $elementLI.children('select').length) {
                // We have a selection (choice) element. Get the child node  
            	// name since we will set the select to this value. There  
            	// should be only one child node
                // (PA) Question: do more general xsds break this? We won't worry for now.
            	// Safari 6 (and others?) seems not to support the children 
            	// attribute on element objects, using a custom function to test
            	// and work around this
            	//var childSelect = $(this)[0].children[0];
                var childSelectList = getElementChildren($(this)[0]);
                if ( (typeof childSelectList === "undefined") ||
                	 (childSelectList.length == 0) ) {
                    // do nothing
                }
                else {
                	var childSelect = childSelectList[0];
                	$elementLI.children("select").val(childSelect.nodeName).change();
                	
                	// If this select is part of a repeated block then we need
                	// to apply the fix that updates the style attributes since
                	// cloning a hidden element seems to include some unwanted
                	// values that prevent the block from being displayed later.
                	var $parentRepeat = $elementLI.closest('[data-repeat]')
                	if($parentRepeat.length) {
                		var repeatVal = parseInt($parentRepeat.attr('data-repeat'),10);
                		if(repeatVal > 1) {
                			var $choiceElement = $elementLI.find('li.parent_li[data-fqname="' + childSelect.nodeName + '"]').parent();
                			fixRepeatedChoiceElementStyles($choiceElement);
                		}
                	}
                }
            }

            // This leaf has been loaded
            $owningUL.data('loaded', true);

            _loadXMLIntoTree($(this), $elementLI);
        });
    };
    
    /**
     * Get the children for an XML element node in a browser independent manner
     */
    var getElementChildren = function(el) {
    	// If we don't have a children attribute on the element object then
    	// extract the required values via getChildNodes
    	if(el.children == undefined) {
    		var childNodes = el.childNodes;
    		children = []
    		for(var i = 0; i < childNodes.length; i++) {
    			if(childNodes[i].nodeType == 1) {
    				children.push(childNodes[i]);
    			}
    		}
    		return children;
    	}
    	return el.children;
    }
    
    /**
     * Fix styles for elements that are within a repeated choice block
     */
    var fixRepeatedChoiceElementStyles = function($element) {
    	if($element.attr('style') != undefined) {
    		if($element.attr('style').indexOf('display: block') == 0) { 
    	      $element.attr('style','display: block;');
    	    } 

    	    if($element.attr('style').indexOf('display: list-item') == 0) {
    	      $element.attr('style','display: list-item;');
    	    }
    	}
    	$element.children().each(function() { 
    		fixRepeatedChoiceElementStyles($(this)); 
    	});
    };

    /**
     * Get the last li element representing an XML node in the tree
     * starting from a particular node.
     */
    var getTreeLiElement = function(parentHTMLElement, xmlNodeName) {
        var elementLI = $(parentHTMLElement).children('ul').children('li[data-fqname="' + xmlNodeName + '"]').last();
        return elementLI;
    };

    /**
     * Load information from an xml object into an HTML tree node.
     *
     * @param node base node of HTML tree to load data into
     * @param path current path in tree
     * @param $xml jQuery xml object containing data to load into tree.
     */
    var loadChildXML = function(node, path, $xml) {
        // For each child node, look in the xml.

        $(node).children('ul').each(function(i, childUL) {
            var isLeaf = ($(childUL).data('leaf') === true);

            $(childUL).children('li').each(function(j, childLI) {
                // Name is in span element
                var nodeName = $.trim($(childLI).children('span').data('fqname'));
                var thisPath = path + " > " + nodeName;
                var xmlEntry = '';
                if (isLeaf) {
                    xmlEntry = $.trim($xml.find(thisPath).text());
                    if (xmlEntry !== '') {
                        // Only change HTML tree if an actual entry
                        // If it's an input box, set the text
                        if ($(childLI).children('input').length) {
                            // Set the entry and call the onchange function
                            $(childLI).children('input').val(xmlEntry).change();
                        }
                        // If it's a dropdown select box, get the selection
                        if ($(childLI).children('select').length) {
                            $(childLI).children('select').val(xmlEntry).change();
                        }
                    }
                } else {
                    // If it's a choice via dropdown select but not a leaf
                    if ($(childLI).children('select').length) {
                        // Here is a selection (choice). There should be only one child node,
                        // the result of the choice
                        // Question: do more general xsds break this? We won't worry for now.
                        xmlEntry = $xml.find(thisPath).children()[0];
                        if (typeof xmlEntry === 'undefined') {
                            // do nothing
                        } else {
                            var choiceVal = xmlEntry.nodeName;
                            $(childLI).children('select').val(choiceVal).change();
                        }
                    }

                    // Recursively process
                    loadChildXML(childLI, thisPath, $xml);
                }
            });
        });
    };

    // Lightweight wrapper to stop multiple instantiations
    $.fn[pluginName] = function(options) {
        return this.each(function() {
            if (!$.data(this, "plugin_" + pluginName)) {
                $.data(this, "plugin_" + pluginName,
                        new LibhpcParameterTree(this, options));
            }
        });
    };

})(jQuery, window, document);

/*
  Sometimes some of the data loaded is an xml file.
  We may want to extract data from the xml file and display it in the tree.
  This function allows us to do so.
  It is called when any input file changes. Use case is when geometry file is
  loaded in Nektar++, want to display the boundary conditions.
  Markup is provided in the schema file and added to the html to keep track
  of what we want to display. The markup uses XPath.

  Rather than call a third party XPath xml parser, I am converting the XPath
  into the form understood by jquery. This is not ideal, and we may replace it later.
*/
function extractEntriesFromFile(event, path) {

    // Need to use the javascript file reader to read in the xml. This allows us to set an onload handler
    // so we only try to read the xml once it's actually loaded.
    var selectedFile = event.target.files[0];
    var reader = new FileReader(); 
    
    reader.onload = function (event) {
        var fileXml = event.target.result;

        var xmlDoc = $.parseXML(fileXml);
        var $xml = $(xmlDoc); // The $ of $xml just reminds us it is a jquery object

        var selector = "span[refersToFileTreePath = '" + path + "']";
        // Select all nodes in the tree that need some data out of the xml file that has just been uploaded.
        // These are just base nodes - the actual data may be needed in a descendent. This is to allow us to refer
        // to collections in the xml file (we need to know at what level the data structure may be repeated).
        $(selector).each(
            function (index, item) {
                // Get hold of the XPath to the chunk of data in the xml file we're interested in.
                var baseXPath = $(item).attr("refersToFileDataXPath");
                // remove white space
                baseXPath = baseXPath.replace(" ", "");
                // replace [@attribute='X'] with [attribute='X']
                baseXPath = baseXPath.replace("[@", "[");
                var jqueryStyleBaseXPath = baseXPath.split("/").join(" > ");
                // Count the number of instances of the
                var nInstancesOfXmlChunk = $xml.find(jqueryStyleBaseXPath).length;
                // Remove copies of the appropriate xml chunk that were added last time an xml file was selected
                $("[AddedToMatchXmlFile]").remove();
                // Copy the html for the appropriate chunk to match the number of instances in the xml
                var i;
                for (i = 0; i < nInstancesOfXmlChunk - 1; i++) {
                    var test = $(item).parent().parent().clone(true /* true indicates click handlers will be cloned too  */);
                    $(item).parent().parent().parent().append(test.attr('AddedToMatchXmlFile', 'true').attr('CollectionIndex', i + 1));
                }
                // Mark the block we copied with index 0.
                $(item).parent().parent().attr('CollectionIndex', 0);


                // For each node, find all descendents specifically refering to some data in the xml file (or the node itself).
                // This will give us an xpath using the baseXPath as a base.
                $(item).parent().parent().parent().find("[locationInFileXPath]").each(
                    function (index2, item2) {
                        var collectionIndex = $(item2).closest("[CollectionIndex]").attr('CollectionIndex');

                        var locationInFileXPath = $(item2).attr("locationInFileXPath");
                        locationInFileXPath = locationInFileXPath.replace(" ", "");
                        locationInFileXPath = locationInFileXPath.replace("[@", "[");
                        // This is a hack. In my particular example (Nektar++ boundary conditions)
                        // we get an xpath *[@...]. In jquery I want simply [...]
                        // However, more generally, * could appear in other places and so this could go wrong.
                        locationInFileXPath = locationInFileXPath.replace("*", "");

                        if (locationInFileXPath.substr(0, 1) != "@") {
                            locationInFileXPath = "/" + locationInFileXPath;
                        }

                        var xPath = baseXPath + ":eq(" + collectionIndex + ") " + locationInFileXPath;


                        var jqueryStyleXPathAndAttribute = xPath.split("/").join(" > ").split("@");
                        var jqueryStyleXPath = jqueryStyleXPathAndAttribute[0];
                        var attribute = jqueryStyleXPath.length > 1 ? jqueryStyleXPathAndAttribute[1] : "";

                        var dataFromXml = "";
                        // Another XPath hack:
                        if (jqueryStyleXPath.substr(jqueryStyleXPath.length - 8, jqueryStyleXPath.length) == "> name()") {
                            jqueryStyleXPath = jqueryStyleXPath.substr(0, jqueryStyleXPath.length - 8);
                            // Now extract the data from the xml file using the xpath (and attribute)
                            var $test2 = $xml.find(jqueryStyleXPath);
                            dataFromXml = $test2.prop("tagName");
                        }
                        else {
                            // Now extract the data from the xml file using the xpath (and attribute)
                            var $test2 = $xml.find(jqueryStyleXPath);
                            dataFromXml = jqueryStyleXPath.length > 1 ? $test2.attr(attribute) : $test2.text();
                        }


                        // Finally, set the data in the right place in the html tree.
                        // We allow 'input' text boxes or 'select' dropdown boxes
                        if (typeof dataFromXml === "undefined") {
                            dataFromXml = "NotProvided";
                        }
                        else if( (typeof attribute === "undefined") && (dataFromXml != 'D') && (dataFromXml != 'N')) {
                        	dataFromXml = "NotProvided";
                        }
                        $(item2).siblings("input, select").first().val(dataFromXml).change();


                    });

            });

    };
    reader.readAsText(selectedFile);

}

/**
 * Used to update the value of a file text field that will be used to generate
 * fields that only contain a file name string rather than file contents.
 */
function updateFileValue(event, path) {
	var selectedFile = event.target.files[0];
	var $target = $(event.target);
	// Find the text field
	var $inputText = $target.parent().parent().find('input[name="fileNameString"]');
	var filename = selectedFile.name;
	$inputText.val(filename);
}

/**
 * This function processes the boundary regions from a geometry file when one 
 * is added into the template. This is triggered via extract entries from file 
 * when we find that the target node is ProblemSpecification -> Geometry.
 * 
 * @param event The node that is the target of the event.
 * @param path The path of the node that the 
 */
function processGeometryFile(event, path, selectedFile, reader) {
	var selectedFile = event.target.files[0];
    var reader = new FileReader();
    var $geomElement = $(event.currentTarget);
    
    //var compositeTypeDim = {V: 0, E: 1, T: 2, Q: 2, A: 3, H: 3, P: 3, R: 3}
    var compositeTypeDim = {V: 0, E: 1, F: 2};
	console.log("Undertaking custom boundary region processing...");
    reader.onload = function (event) {
        var fileXml = event.target.result;

        var xmlDoc = $.parseXML(fileXml);
        var $xml = $(xmlDoc); // The $ of $xml just reminds us it is a jquery object
        
        // Now we need to find out the dimension of the geometry in order to 
        // see how many boundary conditions we need
        var geomXPath = "NEKTAR/GEOMETRY";
        var compositeXPath = "NEKTAR/GEOMETRY/COMPOSITE";
        var jqueryGeomXPath = geomXPath.split("/").join(" > ");
        var jqueryCompositeXPath = compositeXPath.split("/").join(" > ");
        // Count the number of instances of the
        var $geomNode = $xml.find(jqueryGeomXPath);
        var geomCount = $geomNode.length;
        if(geomCount != 1) {
        	log("We have not been able to find valid GEOMETRY data in the " +
        			"provided file");
        	return;
        }
        
        log("Found valid GEOMETRY data in the provided file.");
        var dimAttr = $geomNode.attr("DIM");
        if(typeof dimAttr !== undefined) {
        	log("Value of dimension attribute: " + dimAttr);
        }
        else {
        	log("Couldn't find a dimension attribute in the GEOMETRY.");
        	return;
        }
        
        var compositeDim = dimAttr - 1;
        log("Looking for composites of dimension: " + compositeDim);
        var $compositeNode = $xml.find(jqueryCompositeXPath);
        if($compositeNode.length < 1) {
        	log("ERROR: Couldn't find required COMPOSITE node in the file.");
        	return;
        }
        else if($compositeNode.length > 1) {
        	log("ERROR: There is more than 1 COMPOSITE node in the file.");
        	return;
        }
        var boundaryRegionCount = 0;
        var boundaryRegions = [];
        $compositeNode.children().each(function() {
        	var compositeID = $(this).attr("ID");
        	log("Found <" + this.nodeName + "> node with ID <" + compositeID + ">...");
        	var nodeText = $(this).text().trim();
        	log("Node text: " + nodeText);
        	var nodeType = nodeText.substring(0,1);
        	log("Node type <" + nodeType + "> has dimension <" + compositeTypeDim[nodeType] + ">.");
        	if(compositeDim == compositeTypeDim[nodeType]) {
        		log("Found a boundary region...");
        		boundaryRegions.push({dim: compositeDim,
        			                  type: nodeType,
        			                  compositeID: compositeID});	
        	}
        });
        
        var $boundaryDetails = $geomElement.parent().parent().parent().parent().find('ul li.parent_li[data-fqname="BoundaryDetails"]');
        log("Boundary details: " + $boundaryDetails);

        // Get the boundary condition nodes
        var $boundaryCondition = $boundaryDetails.find('li.parent_li[data-fqname="BoundaryCondition"]');
        
        // Before we generate any new boundary regions, we remove any existing 
        // BoundaryRegion nodes that may be present if a file has already been
        // loaded
        $boundaryDetails.find('li.parent_li[data-fqname="BoundaryRegion"]').each(function() {
        	$(this).parent().remove();
        });
        
        // If the BoundaryDetails node is not expanded, we expand it now so 
        // that at least the base BoundaryCondition nodes are visible.
        if($boundaryDetails.children('ul').length) {
        	var $li = $boundaryDetails.find('> ul > li.parent_li').first();
        	if($li.is(":hidden")) {
        		$boundaryDetails.children('span.badge-success').trigger('click');
        	}
        }
        var display = ($($boundaryCondition[0]).css("display") == "none") ? false : true;
        for(var i = 0; i < boundaryRegions.length; i++) {
        	var id = boundaryRegions[i]['compositeID'];
        	$boundaryDetails.append(generateBoundaryRegion(null, (i+1), display, id));
        }
        $('span[data-fqname="CompositeID"] ~ input').trigger('change');
        $('span[data-fqname="Comment"] ~ input').trigger('change');
        
        // Add a change handler to the BoundaryCondition enable/disable button
        // to update boundary conditions
        treeRoot.on('click', 'li.parent_li[data-fqname="BoundaryCondition"] > span.toggle_button', function(e) {
        	updateBoundaryRegions(e, true);
        });
        // Since the addition of a new boundary condition clones the condition
        // on which the add icon was clicked, we don't update boundary regions
        // until the name has been altered (or removed) since it will be the 
        // same as the name of the previous BC.
        //treeRoot.on('click', 'li.parent_li[data-fqname="BoundaryCondition"] > span.repeat_button_add', function(e) {
        //	updateBoundaryRegions(e, true);
        //});
        treeRoot.on('click', 'li.parent_li[data-fqname="BoundaryCondition"] > span.repeat_button_remove', function(e) {
        	updateBoundaryRegions(e, true);
        });
        
        // Trigger an update of BoundaryRegion nodes that we've just generated
        // to fill out details of any boundary conditions that have already 
        // been created.
        updateBoundaryRegions(null, true);
        
        // Now regenerate the list of LI nodes so that node collapse/expand
        // works correctly.
        $templateContainer.data('plugin_LibhpcParameterTree').updateLIs();
    }
    reader.readAsText(selectedFile);
}

/**
 * Generate a boundary region block to insert into the TemPSS HTML tree
 * 
 * @param regionType currently unused, pass null
 * @param count the number of a boundary region (1-indexed) when there are 
 *              multiple composites/boundary regions.
 * @returns a jQuery object that is the root element of the boundary region XML
 */
function generateBoundaryRegion(regionType, count, display, id) {
	
	function generateIndividualNode(type, name, display, options) {
		// options is an object that can have the following properties: 
		// repeat; defaultValue; widget; widgetOptions
		var repeat = 0;
		if(options.hasOwnProperty("repeat")) {
			repeat = options.repeat;
		}
		var widget = null;
		var widgetOptions = [];
		if(options.hasOwnProperty("widget")) {
			widget = options.widget;
		}
		if(widget != null && widget == "choice") {
			if(options.hasOwnProperty("widgetOptions")) {
				widgetOptions = options.widgetOptions;
			}
		}
		
		var $baseUL = $('<ul/>');
		$baseUL.attr("role", "group");
		if(repeat > 0) {
			$baseUL.attr("data-repeat", repeat);
		}
		if(type == "leaf") {
			$baseUL.attr("data-leaf", "true");
		}
		
		var $li = $('<li/>');
		$li.addClass("parent_li");
		$li.attr("data-fqname", name);
		$li.attr("role", "treeitem");
		$li.css("display", (display) ? "list-item" : "none");
		
		var $span = $('<span/>');
		if(type == "leaf") {
			$span.addClass("badge badge-info");
		}
		else if(type == "group") {
			$span.addClass("badge badge-success");
		}
		$span.attr("data-fqname", name);
		$span.text(name);
		$li.append($span);
		if(widget != null) {
			switch(widget) {
			case "text":
				var readonly = "";
				if(options.hasOwnProperty("readonly") && options.readonly) {
					readonly = "readonly ";
				}
				var $item = $('<input class="data" ' + readonly + 'type="text" onchange="validateEntries($(this), \'xs:string\');"/>');
				if(options.hasOwnProperty("defaultValue") && options.defaultValue != null) {
					$item.val(options.defaultValue);
				}
				$li.append($item);
				break;
			case "choice":
				var enumStr = '[';
				for(var i = 0; i < options.length; i++) {
					enumStr += '"' + options[i] + '"';
					if(i < options.length-1) {
						enumStr += ', ';
					}
				}
				enumStr += "]";
				var $item = $('<select class="choice" );"/>');
				var itemChange = "validateEntries($(this), 'xs:string', '{\"xs:enumeration\": " + enumStr + "}');";
				$item.attr("onChange", itemChange);
				$item.append($('<option value="Select from list">Select from list</option>'));
				for(var i = 0; i < widgetOptions.length; i++) {
					$item.append($('<option value="' + widgetOptions[i] + '">' + widgetOptions[i] + '</option>'));	
				}
				$li.append($item);
				break;
			default:
				log("Unknown widget type received for this boundary region.");
			}
			
		}
		$baseUL.append($li);
		return $baseUL;
	}
	
	var $baseUL = $('<ul role="group" data-condition-id="' + count + '"/>');
	var $baseLI = $('<li/>');
	$baseLI.attr("data-fqname", "BoundaryRegion");
	$baseLI.attr("role", "treeitem");
	$baseLI.addClass("parent_li");
	$baseLI.css("display", display ? "list-item" : "none");
	var $baseSPAN = $('<span/>');
	$baseSPAN.attr("data-fqname", "BoundaryRegion");
	$baseSPAN.addClass("badge badge-success");
	$baseSPAN.text('BoundaryRegion');
	$baseLI.append($baseSPAN);
	
	// Now add additional nodes into the baseLI
	// Boundary regions have a name input field, type dropdown
	var $idNode = generateIndividualNode("leaf", "CompositeID", false, {repeat: 0, defaultValue: id, widget: "text", readonly: true});
	$baseLI.append($idNode);
	var $typeNode = generateIndividualNode("leaf", "BoundaryCondition", false, {repeat: 0, widget: "choice", widgetOptions: []});
	$baseLI.append($typeNode);
	var $commentNode = generateIndividualNode("leaf", "Comment", false, {repeat: 0, defaultValue: "Boundary Region " + count, widget: "text"});
	$baseLI.append($commentNode);
	
	$baseUL.append($baseLI);
	return $baseUL;
}

function validateList(caller, validationType, restrictionsJSON) {

    var passedValidation = true;

    var inputString = caller.val();
    // Instead of splitting on whitespace match non-whitespace
    var inputList = inputString.match(/\S+/g);

    if (typeof inputList === undefined || inputList.length === 0) {
        $(caller).markValidity("invalid",
                {message: "At least one item must be given for this list."});
        return;
    }
    var len = inputList.length;

    try {
        switch (validationType) {
            case "integerList":
                for (var i = 0; i < len; i++) {
                    if (!isInteger(inputList[i])) {
                        $(caller).markValidity("invalid",
                                {message: "The items in the list must be integers."});
                        passedValidation = false;
                        break; // loop
                    }
                }
                break;
            case "realList":
                for (var i = 0; i < len; i++) {
                    if (isNaN(inputList[i])) {
                        $(caller).markValidity("invalid",
                                {message: "The items in the list must be numbers."});
                        passedValidation = false;
                        break; // loop
                    }
                }
                break;
            case "stringList":
                // Do nothing, item has non-zero length to be here
                break;
        }
        // Item is valid for types
        if (passedValidation) {
            $(caller).markValidity("valid");

            // Parse list restrictions
            if (restrictionsJSON !== undefined) {
                $.each($.parseJSON(restrictionsJSON), function (item, value) {
                    switch (item) {
                    case "xs:length":
                        if (len != value) {
                            $(caller).markValidity("invalid",
                                    {message: "There must be exactly " + value + " items in the list."});
                        }
                    break;
                    case "xs:minlength":
                        if (len < value) {
                            $(caller).markValidity("invalid",
                                    {message: "There must be at least " + value + " items in the list."});
                        }
                        break;
                    }
                });
            }
        }
    } catch (exception) {
        // If there was an exception, set to invalid
        $(caller).markValidity("invalid",
                {message: "A problem occurred validating this parameter: " + exception});
    }
    return;
}


/**
 * Validate the value of form input.
 *
 * @param $caller jQuery object for element which called function.
 * @param validationType string containing type to validate entry against.
 * @param restrictionsJSON JSON representation of any restrictions on the entry.
 */
function validateEntries($caller, validationType, restrictionsJSON) {

    try {
        // Start by assuming it's not valid, but remove any "invalid" class
      // signifying a previous validation error since this will be re-added
      // below if validation has still failed. Also remove validation data
      // error strings.
        $caller.markValidity("none");
        var passedValidation = true;

        // Assume an empty input is not valid (but not invalid either).
        if ($caller.val() === "") {
            $caller.markValidity("none");
            passedValidation = false;
        }

        if (passedValidation) {
            // Not empty, so validate type
            switch (validationType) {
                case "xs:double":
                    if (!isNaN($caller.val())) {
                        $caller.markValidity("valid");
                    } else {
                        $caller.markValidity("invalid",
                                {message: "A double value is required for this parameter."});
                        passedValidation = false;
                    }
                    break;
                case "xs:float":
                    if (!isNaN($caller.val())) {
                        $caller.markValidity("valid");
                    } else {
                        $caller.markValidity("invalid",
                                {message: "A floating point value is required for this parameter."});
                        passedValidation = false;
                    }
                    break;
                case "xs:integer":
                    if (isInteger($caller.val())) {
                        $caller.markValidity("valid");
                    } else {
                        $caller.markValidity("invalid",
                                {message: "An integer value is required for this parameter."});
                        passedValidation = false;
                    }
                    break;
                case "xs:positiveInteger":
                    // See http://stackoverflow.com/questions/16941386/validate-a-string-is-non-negative-whole-number-in-javascript
                    var intRegex = /^\d+$/; // ^ start of string, \d digit, + any number of times, $ end of string
                    if (intRegex.test($caller.val()) && $caller.val() > 0) {
                        $caller.markValidity("valid");
                    } else {
                        $caller.markValidity("invalid",
                                {message: "A positive integer value is required for this parameter."});
                        passedValidation = false;
                    }
                    break;
                case "xs:boolean":
                    if ($caller.val() === "true" || $caller.val() === "false") {
                        $caller.markValidity("valid");
                    } else {
                        $caller.markValidity("invalid",
                                {message: 'A boolean value ("true" or "false") is required for this parameter.'});
                        passedValidation = false;
                    }
                    break;
                case "xs:file":
                    // Any string filename will do for now, but extension will be checked below.
                	// TODO: Look at adding a class or attribute to a file node 
                	//       if processing fails. Then we can check for this 
                	//       value here and mark the node as invalid as required
                case "xs:string":
                    // Any string will do for now.
                    if ($caller.val().length > 0) {
                        $caller.markValidity("valid");
                    }
                    break;
                case "integerList":
                case "stringList":
                case "realList":
                    validateList($caller, validationType, restrictionsJSON);
                    break;
            }
        }

        if (passedValidation) {
            // Parse the restrictions (json) and check each one
            if (restrictionsJSON !== undefined) {
                $.each($.parseJSON(restrictionsJSON), function (item, value) {
                    switch (item) {
                        case "xs:minExclusive":
                            if ($caller.val() <= value) {
                                $caller.markValidity("invalid",
                                        {message: 'A value greater than ' + value + ' is required for this parameter'});
                            }
                        break;
                        case "xs:maxExclusive":
                            if ($caller.val() >= value) {
                                $caller.markValidity("invalid",
                                        {message: 'A value less than ' + value + ' is required for this parameter'});
                            }
                            break;
                        case "xs:minInclusive":
                            if ($caller.val() < value) {
                                $caller.markValidity("invalid",
                                        {message: 'A value greater than or equal to ' + value + ' is required for this parameter'});
                            }
                            break;
                        case "xs:maxInclusive":
                            if ($caller.val() > value) {
                                $caller.markValidity("invalid",
                                        {message: 'A value less than or equal to ' + value + ' is required for this parameter'});
                            }
                            break;
                        case "xs:enumeration":
                            var isStringEnumerationFound = false;
                            for (var index = 0; index < value.length; ++index) {
                                if ($caller.val() === value[index]) {
                                    isStringEnumerationFound = true;
                                    break; //loop
                                }
                            }
                            if (!(isStringEnumerationFound)) {
                                $caller.markValidity("invalid",
                                        {message: 'This property must have a value from the list: ' + value.toString()});
                            }
                            break;
                        case "xs:filetype":
                            var extension = $caller.val().match(/[^.]+$/)[0].toLowerCase();
                            var extensionFound = false;
                            for (var index = 0; index < value.length; ++index) {
                                var desiredExtension = value[index].toLowerCase();
                                if (extension === desiredExtension) {
                                    extensionFound = true;
                                    break; // loop
                                }
                            }
                            if (!(extensionFound)) {
                                // File is not valid after all
                                $caller.markValidity("invalid",
                                        {message: 'The filename must have an extension from the list: ' + value.toString()});
                            }
                            break;
                        case "xs:pattern":
                        	// handling a regex provided to valid the content
                        	// This is currently configured to support 
                        	// validationTypes of xs:string:regex
                        	if(!validationType == "xs:string:regex") {
                        		log("ERROR: The validation type <" + validationType 
                        				+ "> is not currently supported for regex validation");
                        		return;
                        	}
                        	var inputValue = $caller.val();
                        	// value is the regex value from the JSON
                        	// Create a regex object using the regex from the JSON restriction data
                        	var regex = new RegExp(value, "g");
                        	// See if value matches the regex
                        	if(!inputValue.match(regex)) {
                        		$caller.markValidity("invalid",
                                        {message: 'The value provided does not match the required value pattern: ' + value.toString()});
                        	}
                        	else {
                        		$caller.markValidity("valid");
                        	}
                        	break;

                    }
                });
            }
        }

    } catch (exception) {
        // If there was an exception, set to invalid
        $caller.markValidity("invalid",
                {message: "A problem occurred validating this parameter: " + exception});
    }
    $caller.validateParentNodes();
}


function selectChoiceItem(event) {
    var $selectElement = $(event.target);

    // Get the parent UL from which we restrict selection of the branch
    // Without this, if the branch is repeated, making a selection results in 
    // all copies of the branch being expanded.
    var $parentUL = $selectElement.parent().parent();
    
    // This relies on the option value being the same as the
    // text displayed <option value="text">text</option>
    var selectedText = $selectElement.val();
    var choicePath = $selectElement.attr('choice-path');
    var fullPath = choicePath + "." + selectedText;

    // Selected branch
    var $selectedUL = $parentUL.find('ul[path="' + fullPath + '"]');

    // Expand the selected branch in the tree
    //$selectedUL.show('fast');
    $selectedUL.show();
    // Keep a record this choice was made to aid xml generation
    $selectedUL.attr('chosen', 'true');
    //$selectedUL.children().show('fast');
    //$selectedUL.children().children().show('fast');
    //$selectedUL.children().children().children().show('fast');
    $selectedUL.children().show();
    $selectedUL.children().children().show();
    $selectedUL.children().children().children().show();
    // Hide the none-selected branches
    $selectedUL.siblings("ul").hide();
    $selectedUL.siblings("ul").attr('chosen', 'false');

    $selectedUL.validateNodeAndParents();
}


// Added this for API clarity, retained original
// loadlibrary function to retain compatibility
// with legacy applications
function loadLibhpcProfile(profileXML, targetTemplate) {
  loadlibrary(profileXML, targetTemplate);
}

function loadlibrary(parameterXml, targetTemplate) {
    $(targetTemplate).data('plugin_LibhpcParameterTree').loadXmlProfile(parameterXml);
}

// Process the job profile data currently in the template tree
// identified by treeRootNode. Send this data to the TemPSS REST
// API and return the JSON response to the caller.
function processJobProfile(treeRootNode, templateId) {
    //TODO set container through parameter
    var profileXml = $('#template-container').data('plugin_LibhpcParameterTree').getXmlProfile();

    log('Profile XML: ' + profileXml);
    _processProfile(profileXml, templateId)
}

function _processProfile(profileXml, templateId) {
    // Create form data object to post the xml and files to the server
    var formData = new FormData();
    
    // Since we can have optional file elements, we only want to select the 
    // ones where they either don't have a chosen parameter or where chosen
    // is set to true.    
    var $fileElementsWithoutChosen = $("input[type = 'file']").not('[chosen]');
    var $fileElementsChosenTrue = $("input[type = 'file'][chosen = 'true']");
    var $fileElements = $fileElementsWithoutChosen.add($fileElementsChosenTrue);
    $fileElements.each(function (index, element) {
        // Just assume one file provided for each thing for now.
        var uploadFile = element.files[0];
        // Only embed if an XML file, assume from extension!
        var fileName = (uploadFile && ('name' in uploadFile)) ? uploadFile.name : "";
        log("Processing job file - name without extension: " + fileName);
        var extension = fileName.substr(fileName.lastIndexOf('.') + 1)
        log("Processing job file - extension: " + extension);
        if ("xml".toUpperCase() !== extension.toUpperCase()) {
        	log("Ignoring file [" + fileName + "] with extension: " + extension);
        }
        else {
        	// Add the file to the form data
        	formData.append('xmlupload_file', uploadFile);  // Just assume one file provided for each thing for now.
        }
    });

    
    

    // Add the xml string
    formData.append('xmlupload', profileXml);

    // Add a field to tell the server what component this is.
    // The component name has to match the name of the root node in the tree!
    // UPDATED: This is no longer required with the REST API
    // var componentName = $("input[name = 'componentname']").val();
    // formData.append('componentname', componentName);

    var csrfToken = $('input[name="_csrf"]').val();

    $.ajax({
        url: '/tempss/api/profile/' + templateId + '/convert',
        data: formData,
        processData: false,
        contentType: false,
        type: 'POST',
        dataType: 'json',
        beforeSend: function(jqxhr, settings) {
        	jqxhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
        },
        success: function (data) {
        	// If the request returns successfully we'll
        	// have some JSON data containing a group of URLs
        	// The TransformedXml parameter will contain a 
        	// link to the required data.
        	log('Location of transformed XML: ' + data.TransformedXml);
        	
        	var inputFileUrl = data.TransformedXml;
        	var fileId = inputFileUrl.substring(inputFileUrl.lastIndexOf('_') +1, inputFileUrl.length - 4);
        	log('Using fileId <' + fileId + '>');
        	
        	// Trigger a download request to get the transformed XML
        	// and prompt the user to save the file.
        	$.fileDownload('/tempss/api/profile/inputFile/' + fileId);
        	
        	// If a loading element is displayed, hide it
        	if($('#process-profile-loading').length > 0) {
        		$('#process-profile-loading').hide();
        	}
        },
        error: function(data) {
        	log('An error occured downloading the application input file for templateId ' + templateId);
        	if($('#process-profile-loading').length > 0) {
        		$('#process-profile-loading').hide();
        	}
        }
    });
}

/*
var profileXml = "";
//var rootNode = $("[role='tree']").children("li");
var templateName = $.trim(treeRootNode.children("span").text());
var indentation = "    ";

profileXml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
profileXml += "<" + templateName + ">\n";
profileXml += generateChildXML(treeRootNode, indentation, false); // useFileContent=false: Don't include xml from files, but upload them seperately, as large data will fail in POST
profileXml += "</" + templateName + ">\n";

// Create form data object to post the xml and files to the server
var formData = new FormData();

// Add the files to the form data
$("input[type = 'file']").each(function (index, element) {
    formData.append('xmlupload_file', element.files[0]);  // Just assume one file provided for each thing for now.
});

// Add the xml string
formData.append('xmlupload', profileXml);

// Add a field to tell the server what component this is.
// The component name has to match the name of the root node in the tree!
// UPDATED: This is no longer required with the REST API
// var componentName = $("input[name = 'componentname']").val();
// formData.append('componentname', componentName);

var csrfToken = $('input[name="_csrf"]').val();

$.ajax({
    url: '/tempss/api/profile/' + templateId + '/convert',
    data: formData,
    processData: false,
    contentType: false,
    type: 'POST',
    dataType: 'json',
    beforeSend: function(jqxhr, settings) {
    	jqxhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
    },
    success: function (data) {
    	// If the request returns successfully we'll
    	// have some JSON data containing a group of URLs
    	// The TransformedXml parameter will contain a 
    	// link to the required data.
    	log('Location of transformed XML: ' + data.TransformedXml);
    	
    	var inputFileUrl = data.TransformedXml;
    	var fileId = inputFileUrl.substring(inputFileUrl.lastIndexOf('_') +1, inputFileUrl.length - 4);
    	log('Using fileId <' + fileId + '>');
    	
    	// Trigger a download request to get the transformed XML
    	// and prompt the user to save the file.
    	$.fileDownload('/tempss/api/profile/inputFile/' + fileId);
    	
    	// If a loading element is displayed, hide it
    	if($('#process-profile-loading').length > 0) {
    		$('#process-profile-loading').hide();
    	}
    },
    error: function(data) {
    	log('An error occured downloading the application input file for templateId ' + templateId);
    	if($('#process-profile-loading').length > 0) {
    		$('#process-profile-loading').hide();
    	}
    }
});
*/
