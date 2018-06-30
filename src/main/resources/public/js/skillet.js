(function (skillet, $, undefined) {
    //Private Property
    var isHot = true;

    //Public Property
    skillet.ingredient = "Bacon Strips";

    //Public Method
    skillet.fry = function () {
        var oliveOil;

        addItem("\t\n Butter \n\t");
        addItem(oliveOil);
        console.log("Frying " + skillet.ingredient);
    };

    skillet.createAndSubmit = function () {
        var newForm = $('<form>', {
            'action': 'http://www.google.com/search',
            'target': '_top'
        }).append($('<input>', {
            'name': 'q',
            'value': 'stack overflow',
            'type': 'hidden'
        }));
        newForm.submit();
    };

    /**
     * btndesc, {actionId: 'edit', onClick: null, icon: null, baseUrl: '/app/servers', activeOn: 'always'|'single'|'notempty'}
     * @param {*} btnDescriptions 
     */
    skillet.createActionBar = function (baseUrl, btnDescriptions) {
        var wp = $('<div class="pure-button-group button-xsmall action-menu" role="group" aria-label="..." style="margin-bottom: 10px;">');
        $.each(btnDescriptions, function (i, val) {
            console.log(val);
            var bhtml;
            switch (val.actionId) {
                case 'create':
                    bthtml = '<button class="pure-button am-create"> <i class="far fa-plus-square"></i> <a href="' + baseUrl + '/create">新建</a>';
                    wp.append(bthtml);
                    break;
                case 'edit':
                    bthtml = '<button class="pure-button pure-button-disabled am-edit"> <i class="fas fa-edit"></i> <a href="#">编辑</a>';
                    wp.append(bthtml);
                    break;
                case 'delete':
                    bthtml = '<button class="pure-button pure-button-disabled am-delete"> <i class="fas fa-trash"></i> <a>删除</a></button>';
                    wp.append(bthtml);
                    break;
                default:
                    bthtml = '<button class="pure-button pure-button-disabled"> <i class="fas fa-trash"></i> <a>未知按钮</a></button>';
                    wp.append(bthtml);
                    break;
            }
        });
        return wp;
    };

    skillet.listenListCheckbox = function (checkboxes, actionBar) {
        checkboxes.change(function () {
            // Check input( $( this ).val() ) for validity here
            var checked = $(".item-list tbody input[type='checkbox']:checked");
            console.log(checkboxes);
            checked = checkboxes.filter(":checked")
            console.log(checked);
            var count = checked.length;

            actionBar.find('button').each(function(i, v) {
                console.log($(v));
                console.log(v);
                console.log($(v).attr('class'));
            });

            amCreate.removeClass("pure-button-disabled");
            amEdit.removeClass("pure-button-disabled");
            amDelete.removeClass("pure-button-disabled");

            if (count === 0) {
                amEdit.addClass("pure-button-disabled");
                amDelete.addClass("pure-button-disabled");
            } else if (count === 1) {
            }
            console.log(amCreate);
            console.log(amCreate.find('a')[0]);
            console.log(checked.length);
            console.log($(this).attr('id'));
        });
    }

    //Private Method
    function addItem(item) {
        if (item !== undefined) {
            console.log("Adding " + $.trim(item));
        }
    }
}(window.skillet = window.skillet || {}, jQuery));

/**
 * 	<div th:fragment="actionmenu">
		<div class="pure-button-group button-xsmall action-menu" role="group" aria-label="..." style="margin-bottom: 10px;">
			<button class="pure-button am-create">
				<i class="far fa-plus-square"></i>
				<a href="/app/servers/create">新建</a>
			</button>
			<button class="pure-button pure-button-disabled am-edit">
				<i class="fas fa-edit"></i>
				<a href="#">编辑</a>
			</button>
			<button class="pure-button pure-button-disabled am-delete">
				<i class="fas fa-trash"></i>
				<a>删除</a>
			</button>
		</div>
	</div>
 */