const clearForm = form => {
    $(form + ' .title').val('')
    $(form + " .message").val('')
    $(form + " .amount_of_time").val('INFINITE')
    if (form === '#drop-down') {
        $("#coords__x").val('')
        $("#coords__y").val('')
    }
}

const openPopup = (popup) => {
    $('.' + popup).fadeIn(300)
    $('html').addClass('no-scroll')
}

toastr.options = {
    'closeButton': true,
    'debug': false,
    'newestOnTop': true,
    'progressBar': true,
    'positionClass': 'toast-bottom-left',
    'preventDuplicates': false,
    'onclick': null,
    'showDuration': 300,
    'hideDuration': 1000,
    'timeOut': 5000,
    'extendedTimeOut': 10000,
    'showEasing': 'swing',
    'hideEasing': 'linear',
    'showMethod': 'fadeIn',
    'hideMethod': 'fadeOut',
    'tapToDismiss': false
}

const closePopup = (form) => {
    if (form === '#drop-down') {
        $('#new-bin-checkbox').prop('checked', false)
    } else if (form === '#pop-up') {
        $('.popup-create-bg').fadeOut(200)
        $('.popup-show-bg').fadeOut(200)
        $('html').removeClass('no-scroll')
    }
}

const showBin = (data) => {
    $('.show__title').text(data.title)
    $('.show__message').text(data.message)
    $('.info_username').text(data.username)
    $('.info_expirationTime').text(defineExpirationTime(data.expirationTime))
    $('.info_coords').text(`(${data.x};${data.y})`)
    $('.info_id_text').text(data.id)

    openPopup('popup-show-bg')
}

const defineExpirationTime = (time) => {
    if (time === null) return 'Infinite'
    const dateMil = Date.parse(time) - Date.now()

    const seconds = Math.floor(dateMil / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (days > 0) return days + ' day' + definePlural(days)
    else if (hours > 0) return hours + ' hour' + definePlural(hours)
    else if (minutes > 0) return minutes + ' minute' + definePlural(minutes)
    else if (seconds > 0) return seconds + ' second' + definePlural(seconds)
}

const definePlural = (quantity) => {
    return quantity > 1 ? 's' : ''
}

const fillTimeOptions = () => {
    const optionText = ['Never', '1 Minute', '10 Minutes', '1 Hour', '1 Day', '1 Week', '1 Month', '6 Month']
    const optionValues = ['INFINITE', 'ONE_MINUTE', 'TEN_MINUTES', 'ONE_HOUR', 'ONE_DAY',
        'ONE_WEEK', 'ONE_MONTH', 'SIX_MONTHS']
    const select = $('.amount_of_time')

    for (let i = 0; i < optionText.length; i++) {
        let option = $('<option></option>').attr('value', optionValues[i]).text(optionText[i])
        select.append(option)
    }
}

function adjustUpperMenuWidth() {
    const blank = $('.upper_menu .blank')
    const authorization = $('.upper_menu .authorization')
    blank.width(authorization.width() + 50)
}

$('#new-bin-checkbox').change(() => {
    let inputs = $('#new-bin-menu input, #new-bin-menu select, #new-bin-menu textarea, #new-bin-menu button')
    if ($('#new-bin-checkbox').is(':checked')) {
        setTimeout(() => inputs.removeAttr('tabindex'), 250)
    } else {
        inputs.attr('tabindex', '-1')
    }
})

$('#coords__input, #coords__auto').change(() => {
    let x = $('#coords__x')
    let y = $('#coords__y')
    if ($('.coords input[type=radio]:checked').attr('id') === 'coords__auto') {
        x.prop('disabled', true)
        y.prop('disabled', true)
        x.removeClass("__req __error")
        y.removeClass("__req __error")
    } else {
        x.prop('disabled', false)
        y.prop('disabled', false)
        x.addClass("__req")
        y.addClass("__req")
    }
})

$('.close-popup').click(() => {
    closePopup('#pop-up')
})

$('.popup-create-bg').click(e => {
    if (e.target.className === 'popup-create-bg' && confirm('Are you sure?')) {
        closePopup('#pop-up')
    }
})

$('.popup-show-bg').click(e => {
    if (e.target.className === 'popup-show-bg') {
        closePopup('#pop-up')
    }
})