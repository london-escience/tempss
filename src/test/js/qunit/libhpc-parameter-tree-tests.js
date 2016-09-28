var treeHTML = '<ul id="top_ul" role="tree">' +
               '<li class="parent_li" role="treeitem" data-fqname="ProblemSetup">' +
               '<span class="badge badge-success" data-fqname="ProblemSetup">ProblemSetup</span>' +
               '<ul id="middle_ul" role="group">' +
               '<li class="parent_li" role="treeitem" data-fqname="PositiveIon">' +
               '<span class="badge badge-warning" data-fqname="PositiveIon">PositeveIon</span>' +
               '<select id="select" class="choice" choice-path="GROMACS_genion.ProblemSetup.PositiveIon">' +
               '<option>Select from list</option>' +
               '<option>Default</option>' +
               '<option>Specified</option>' +
               '</select>' +
               '<ul id="choice_ul" choice-id="Select from list" role="group" chosen="true"></ul>' +
               '<ul id="default_ul" data-leaf="true" role="group" chosen="false">' +
               '<li id="default_li" class="parent_li" role="treeitem" data-fqname="Default">' +
               '<span id="default_span" class="badge badge-info" title="My default test element" data-fqname="Default" data-optional="false">Default</span>' +
               '</li>' +
               '</ul>' +
               '<ul id="specified_ul" data-leaf="true" role="group" chosen="false">' +
               '<li id="specified_li" class="parent_li" role="treeitem" data-fqname="Specified">' +
               '<span id="specified_span" class="badge badge-info" title="My test element" data-fqname="Specified" data-optional="false">Specified</span>' +
               '<input id="specified_input" class="data" type="text">' +
               '</li>' +
               '</ul>' +
               '</li>' +
               '</ul>' +
               '</li>' +
               '</ul>';

var validClassName = 'valid';
var invalidClassName = 'invalid';

function toggleChosenElement(chosenSelector) {
    $(chosenSelector).attr('chosen', 'true').siblings('ul').attr('chosen', 'false');
}

// Custom assertions
QUnit.assert.hasClass = function(selector, testClass, message) {
    var actual = $(selector).hasClass(testClass);
    this.push(actual, actual, selector, message);
};

QUnit.assert.lacksClass = function(selector, testClass, message) {
    var actual = !($(selector).hasClass(testClass));
    this.push(actual, actual, selector, message);
};

QUnit.assert.allParentULsValidityNone = function() {
    var none = (
            !($('#top_ul').hasClass(validClassName)) &&
            !($('#middle_ul').hasClass(validClassName)) &&
            !($('#top_ul').hasClass(invalidClassName)) &&
            !($('#middle_ul').hasClass(invalidClassName))
            );

    this.push(none, none, true, 'No parent uls are valid or invalid');
};

QUnit.assert.allParentULsValidityValid = function() {
    var allValid = (
            $('#top_ul').hasClass(validClassName) &&
            $('#middle_ul').hasClass(validClassName) &&
            !($('#top_ul').hasClass(invalidClassName)) &&
            !($('#middle_ul').hasClass(invalidClassName))
            );

    this.push(allValid, allValid, true, 'All parent uls are valid');
};

QUnit.assert.allParentULsValidityInvalid = function() {
    var allInvalid = (
            $('#top_ul').hasClass(invalidClassName) &&
            $('#middle_ul').hasClass(invalidClassName) &&
            !($('#top_ul').hasClass(validClassName)) &&
            !($('#middle_ul').hasClass(validClassName))
            );

    this.push(allInvalid, allInvalid, true, 'All parent uls are invalid');
};

QUnit.assert.validityValid = function(selector) {
    var valid = (
            $(selector).hasClass(validClassName) &&
            !($(selector).hasClass(invalidClassName))
            );

    this.push(valid, valid, true, 'Element ' + selector + ' should be valid');
};

QUnit.assert.validityInvalid = function(selector) {
    var invalid = (
            $(selector).hasClass(invalidClassName) &&
            !($(selector).hasClass(validClassName))
            );

    this.push(invalid, invalid, true, 'Element ' + selector + ' should be invalid');
};

QUnit.assert.validityNone = function(selector) {
    var none = (
            !($(selector).hasClass(validClassName)) &&
            !($(selector).hasClass(invalidClassName))
            );

    this.push(none, none, true, 'Element ' + selector + ' should be neither valid nor invalid');
};

// Tests for the jquery extensions
QUnit.module("jQuery parameter tree plugin - markValidity", {
    beforeEach: function() {
        // Add the required objects to the test fixture
        $('#qunit-fixture').append(
                '<ul id="test_ul" data-leaf="true" role="group">' +
                '<li id="test_li" class="parent_li" role="treeitem">' +
                '<span id="test_span" class="badge badge-info" title="My test element" data-fqname="MyTestElement" data-optional="false">MyTestElement</span>' +
                '<input id="test_input" class="data" type="text" style="display: inline;">' +
                '</li>' +
                '</ul>');
    }
});

QUnit.test("Available", function(assert) {
    assert.ok($.fn.markValidity, 'Function available');
    assert.ok($('#test_input').markValidity('valid'), 'Can be used');
});

QUnit.test("Chainable", function(assert) {
    assert.ok($('#test_input').markValidity('valid').addClass('testing'), 'Can be chained');
    assert.ok($('#test_input').hasClass('testing'), "Class was added correctly from chaining");
});

QUnit.test('Mark Valid', function(assert) {
    assert.expect(4);
    // Test event is triggered OK
    $('#test_ul').on('nodeValid', function() {
        assert.ok(true, 'nodeValid event fired');
    });
    assert.ok($('#test_input').markValidity('valid'), 'Can be used');
    assert.validityValid('#test_ul');
    // Test that tooltip icon has been removed
    assert.notOk($('#test_li > i').length, "Doesn't have tooltip icon");
});

QUnit.test('Mark Invalid', function(assert) {
    assert.expect(4);
    // Test event is triggered OK
    $('#test_ul').on('nodeInvalid', function() {
        assert.ok(true, 'nodeInvalid event fired');
    });
    assert.ok($('#test_input').markValidity('invalid'), 'Can be used');
    assert.validityInvalid('#test_ul');
    // Test that tooltip icon has been added
    assert.ok($('#test_li > i').length, 'Has tooltip icon');
});

QUnit.test('Mark None', function(assert) {
    assert.expect(4);
    // Test event is triggered OK
    $('#test_ul').on('nodeInvalid', function() {
        assert.ok(true, 'nodeInvalid event fired');
    });
    assert.ok($('#test_input').markValidity('none'), 'Can be used');
    assert.validityNone('#test_ul');
    // Test that tooltip icon has been removed
    assert.notOk($('#test_li > i').length, "Doesn't have tooltip icon");
});

QUnit.test('Change validity', function(assert) {
    assert.expect(20);
    // Test events are triggered OK
    $('#test_ul').on('nodeValid', function() {
        assert.ok(true, 'nodeValid event fired');
    });
    $('#test_ul').on('nodeInvalid', function() {
        assert.ok(true, 'nodeInvalid event fired');
    });

    // Mark valid
    assert.ok($('#test_input').markValidity('valid'), 'Can be used');
    assert.validityValid('#test_ul');
    // Test that tooltip icon has been removed
    assert.notOk($('#test_li > i').length, "Doesn't have tooltip icon");

    // Mark invalid
    assert.ok($('#test_input').markValidity('invalid'), 'Can be used');
    assert.validityInvalid('#test_ul');
    // Test that tooltip icon has been added
    assert.ok($('#test_li > i').length, 'Has tooltip icon');

    // Mark valid again
    assert.ok($('#test_input').markValidity('valid'), 'Can be used');
    assert.validityValid('#test_ul');
    // Test that tooltip icon has been removed
    assert.notOk($('#test_li > i').length, "Doesn't have tooltip icon");

    // Mark invalid again
    assert.ok($('#test_input').markValidity('invalid'), 'Can be used');
    assert.validityInvalid('#test_ul');
    // Test that tooltip icon has been added
    assert.ok($('#test_li > i').length, 'Has tooltip icon');

    // Mark none
    assert.ok($('#test_input').markValidity('none'), 'Can be used');
    assert.validityNone('#test_ul');
    // Test that tooltip icon has been removed
    assert.notOk($('#test_li > i').length, "Doesn't have tooltip icon");
});

QUnit.module("jQuery parameter tree plugin - validateNodeAndParents", {
    beforeEach: function() {
        // Add the required objects to the test fixture
        $('#qunit-fixture').append(treeHTML);
    }
});

QUnit.test("Available", function(assert) {
    assert.ok($.fn.validateNodeAndParents, 'Plugin available');
    assert.ok($('#specified_ul').validateNodeAndParents(), 'Can be used');
});

QUnit.test("Chainable", function(assert) {
    assert.ok($('#specified_ul').validateNodeAndParents().addClass('testing'), 'Can be chained');
    assert.hasClass('#specified_ul', 'testing', 'Class was added correctly from chaining');
});

QUnit.test('Check choice node and parents are not marked as valid', function(assert) {
    assert.ok($('#choice_ul').validateNodeAndParents(), 'Can be used');
    assert.validityNone('#choice_ul');
    assert.allParentULsValidityNone();
});

QUnit.test('Check empty data-leaf is marked as valid', function(assert) {
    toggleChosenElement('#default_ul');
    assert.ok($('#default_ul').validateNodeAndParents(), 'Can be used');
    assert.validityValid('#default_ul');
    assert.allParentULsValidityValid();
});

QUnit.test('Check specified node is marked as neither valid or invalid', function(assert) {
    toggleChosenElement('#specified_ul');
    assert.ok($('#specified_ul').validateNodeAndParents(), 'Can be used');
    assert.validityNone('#specified_ul');
    assert.allParentULsValidityNone();
});

QUnit.test('Check specified node stays marked as valid', function(assert) {
    toggleChosenElement('#specified_ul');
    $('#specified_ul').addClass('valid');
    assert.ok($('#specified_ul').validateNodeAndParents(), 'Can be used');
    assert.validityValid('#specified_ul');
    assert.allParentULsValidityValid();
});

QUnit.test('Check specified node stays marked as invalid', function(assert) {
    toggleChosenElement('#specified_ul');
    $('#specified_ul').addClass('invalid');
    assert.ok($('#specified_ul').validateNodeAndParents(), 'Can be used');
    assert.validityInvalid('#specified_ul');
    // Parents of an invalid node are not marked as invalid too
    assert.allParentULsValidityNone();
});

QUnit.test('Changing validity', function(assert) {
    // Chosen node is chosen by default.
    assert.ok($('#choice_ul').validateNodeAndParents(), 'Can be used');
    assert.validityNone('#choice_ul');
    assert.validityNone('#default_ul');
    assert.validityNone('#specified_ul');
    assert.allParentULsValidityNone();
    // Make default one chosen - parents should be valid
    toggleChosenElement('#default_ul');
    assert.ok($('#default_ul').validateNodeAndParents(), 'Can be used');
    assert.validityNone('#choice_ul');
    assert.validityValid('#default_ul');
    assert.validityNone('#specified_ul');
    assert.allParentULsValidityValid();
    // Make third one chosen
    toggleChosenElement('#specified_ul');
    assert.ok($('#specified_ul').validateNodeAndParents(), 'Can be used');
    assert.validityNone('#choice_ul');
    assert.validityValid('#default_ul');
    assert.validityNone('#specified_ul');
    assert.allParentULsValidityNone();
    // Now make third one valid
    $('#specified_ul').addClass('valid');
    assert.ok($('#specified_ul').validateNodeAndParents(), 'Can be used');
    assert.validityNone('#choice_ul');
    assert.validityValid('#default_ul');
    assert.validityValid('#specified_ul');
    assert.allParentULsValidityValid();
    // Now make third one invalid
    $('#specified_ul').removeClass('valid').addClass('invalid');
    assert.ok($('#specified_ul').validateNodeAndParents(), 'Can be used');
    assert.validityNone('#choice_ul');
    assert.validityValid('#default_ul');
    assert.validityInvalid('#specified_ul');
    assert.allParentULsValidityNone();
    // Choose default one again
    toggleChosenElement('#default_ul');
    assert.ok($('#default_ul').validateNodeAndParents(), 'Can be used');
    assert.validityNone('#choice_ul');
    assert.validityValid('#default_ul');
    assert.validityInvalid('#specified_ul');
    assert.allParentULsValidityValid();
    // Chose first one again
    toggleChosenElement('#choice_ul');
    assert.ok($('#choice_ul').validateNodeAndParents(), 'Can be used');
    assert.validityNone('#choice_ul');
    assert.validityValid('#default_ul');
    assert.validityInvalid('#specified_ul');
    assert.allParentULsValidityNone();
});


QUnit.module("jQuery parameter tree plugin - validateParentNodes", {
    beforeEach: function() {
        // Add the required objects to the test fixture
        $('#qunit-fixture').append(treeHTML);
        // Remove chosen attributes
        $('#choice_ul').removeAttr('chosen');
        $('#default_ul').removeAttr('chosen');
        $('#specified_ul').removeAttr('chosen');
    }
});

QUnit.test("Available", function(assert) {
    assert.ok($.fn.validateParentNodes, 'Plugin available');
    assert.ok($('#specified_input').validateParentNodes(), 'Can be used');
});

QUnit.test("Chainable", function(assert) {
    assert.ok($('#specified_input').validateParentNodes().addClass('testing'), 'Can be chained');
    assert.hasClass('#specified_input', 'testing', 'Class was added correctly from chaining');
});

QUnit.test('Check parents not valid when no children valid', function(assert) {
    assert.ok($('#choice_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityNone();
});

QUnit.test('Check parents invalid when first child valid', function(assert) {
    // Now make one valid
    $('#choice_ul').addClass('valid');
    assert.ok($('#choice_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityNone();
});

QUnit.test('Check parents invalid when second child valid', function(assert) {
    // Now make one valid
    $('#default_ul').addClass('valid');
    assert.ok($('#default_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityNone();
});

QUnit.test('Check parents invalid when third child valid', function(assert) {
    // Now make one valid
    $('#specified_ul').addClass('valid');
    assert.ok($('#specified_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityNone();
});

QUnit.test('Check parents invalid when first and second child valid', function(assert) {
    // Now make two valid
    $('#choice_ul').addClass('valid');
    $('#default_ul').addClass('valid');
    assert.ok($('#choice_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityNone();
});

QUnit.test('Check parents invalid when first and third child valid', function(assert) {
    // Now make two valid
    $('#choice_ul').addClass('valid');
    $('#specified_ul').addClass('valid');
    assert.ok($('#choice_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityNone();
});

QUnit.test('Check parents invalid when second and third child valid', function(assert) {
    // Now make two valid
    $('#default_ul').addClass('valid');
    $('#specified_ul').addClass('valid');
    assert.ok($('#choice_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityNone();
});

QUnit.test('Check parents valid when all children valid', function(assert) {
    // Now make all valid
    $('#choice_ul').addClass('valid');
    $('#default_ul').addClass('valid');
    $('#specified_ul').addClass('valid');
    assert.ok($('#choice_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityValid();
});

QUnit.test('Check parents valid when one valid and others not chosen', function(assert) {
    // Make one valid and chosen others not chosen.
    $('#choice_ul').addClass('valid');
    toggleChosenElement('#choice_ul');
    assert.ok($('#choice_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityValid();
});

QUnit.test('Check parents valid when one valid and others disabled', function(assert) {
    // Make one valid and others disabled
    $('#choice_ul').addClass('valid').attr('chosen', 'true');
    $('#default_ul').data('disabled', true);
    $('#specified_ul').data('disabled', true);
    assert.ok($('#choice_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityValid();
});

QUnit.test('Changing validity', function(assert) {
    // Make one valid and others chosen.
    $('#choice_ul').addClass('valid');
    toggleChosenElement('#choice_ul');
    assert.ok($('#choice_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityValid();
    // Make another one chosen, but original still valid
    toggleChosenElement('#default_ul');
    assert.ok($('#default_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityNone();
    // Make second one now valid
    $('#default_ul').addClass('valid');
    assert.ok($('#default_ul').validateParentNodes(), 'Can be used');
    assert.allParentULsValidityValid();
});


// hasInput
QUnit.module("jQuery parameter tree plugin - hasInput", {
    beforeEach: function() {
        // Add the required objects to the test fixture
        $('#qunit-fixture').append(treeHTML);
    }
});

QUnit.test("Available", function(assert) {
    assert.ok($.fn.hasInput, 'Plugin available');
});

QUnit.test("True for element with input", function(assert) {
    assert.ok($('#specified_ul').hasInput(), 'Ul has input');
});

QUnit.test("False for element without input", function(assert) {
    assert.notOk($('#default_ul').hasInput(), 'Ul does not have input');
});

// Tests for utility functions

QUnit.module("Utility functions - isInteger");

QUnit.test( "Integer true", function( assert ) {
    assert.ok(isInteger(1) , "Number 1 should be integer");
});

QUnit.test( "String integer true", function( assert ) {
    assert.ok(isInteger("1"), "String 1 should be integer");
});

QUnit.test( "Float false true", function( assert ) {
    assert.notOk(isInteger(1.1), "Float should not be integer");
});

QUnit.test( "Float string false", function( assert ) {
    assert.notOk(isInteger("1.1"), "Float string should not be integer");
});

QUnit.test( "String false", function( assert ) {
    assert.notOk(isInteger("String"), "String should not be integer");
});

// Tests for tree class

QUnit.module("Libhpc Tree", {
    beforeEach: function() {
        // Add the required objects to the test fixture
        $('#qunit-fixture').append(treeHTML);
        this.tree = $('#qunit-fixture').LibhpcParameterTree();
    },
    afterEach: function() {
        $('#qunit-fixture').data('plugin_LibhpcParameterTree').destroy();
    }
});

QUnit.test("Can instantiate tree", function(assert) {
    assert.ok(this.tree, "Tree exists");
});

QUnit.test("setLeavesWithoutInputsToValid", function(assert) {
    $('.valid').removeClass('valid');
    assert.equal($('.valid').length, 0, "No valid elements");
    $('#qunit-fixture').data('plugin_LibhpcParameterTree').setLeavesWithoutInputsToValid();
    assert.equal($('.valid').length, 1, "One valid element");
    assert.hasClass('#default_ul', 'valid', 'Leaf is marked as valid');
});

QUnit.test("collapseTree", function(assert) {
    $('#qunit-fixture').find('li').show();
    assert.equal($('#qunit-fixture').find('li:visible').length, 4, "All li visible");
    $('#qunit-fixture').data('plugin_LibhpcParameterTree').collapseTree();
    assert.equal($('#qunit-fixture').find('li:visible').length, 2, "Only tree and top levels visible");
});

QUnit.test("expandTree", function(assert) {
    assert.equal($('#qunit-fixture').find('li:visible').length, 2, "Only tree and top levels visible");
    $('#qunit-fixture').find('li').hide();
    assert.equal($('#qunit-fixture').find('li:visible').length, 0, "No li visible");
    $('#qunit-fixture').data('plugin_LibhpcParameterTree').expandTree();
    assert.equal($('#qunit-fixture').find('li:visible').length, 4, "All li visible");
});

QUnit.test("getXmlProfile, none selected", function(assert) {
    var actualXmlString = $('#qunit-fixture').data('plugin_LibhpcParameterTree').getXmlProfile();
    var actualXml = $.parseXML(actualXmlString);
    var expectedXmlString =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<ProblemSetup>\n" +
        "    <PositiveIon>\n" +
        "    </PositiveIon>\n" +
        "</ProblemSetup>\n";
    var expectedXml = $.parseXML(expectedXmlString);
    assert.deepEqual(actualXml, expectedXml, "Extracted XML object is as expected");
    assert.deepEqual(actualXmlString, expectedXmlString, "Extracted XML string is as expected");
});

QUnit.test("getXmlProfile, default selected", function(assert) {
    toggleChosenElement('#default_ul');
    var actualXmlString = $('#qunit-fixture').data('plugin_LibhpcParameterTree').getXmlProfile();
    var actualXml = $.parseXML(actualXmlString);
    var expectedXmlString =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<ProblemSetup>\n" +
        "    <PositiveIon>\n" +
        "        <Default></Default>\n" +
        "    </PositiveIon>\n" +
        "</ProblemSetup>\n";
    var expectedXml = $.parseXML(expectedXmlString);
    assert.deepEqual(actualXml, expectedXml, "Extracted XML object is as expected");
    assert.deepEqual(actualXmlString, expectedXmlString, "Extracted XML string is as expected");
});

QUnit.test("getXmlProfile, select second item", function(assert) {
    toggleChosenElement('#specified_ul');
    $('#specified_input').val('My Value');
    var actualXmlString = $('#qunit-fixture').data('plugin_LibhpcParameterTree').getXmlProfile();
    var actualXml = $.parseXML(actualXmlString);
    var expectedXmlString =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<ProblemSetup>\n" +
        "    <PositiveIon>\n" +
        "        <Specified>My Value</Specified>\n" +
        "    </PositiveIon>\n" +
        "</ProblemSetup>\n";
    var expectedXml = $.parseXML(expectedXmlString);
    assert.deepEqual(actualXml, expectedXml, "Extracted XML object is as expected");
    assert.deepEqual(actualXmlString, expectedXmlString, "Extracted XML string is as expected");
});
