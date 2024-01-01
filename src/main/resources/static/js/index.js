
const extractData = (form) => {
    form = '#' + form
    if(!validateForm(form)) {
        return
    }

    let title = $(form + " .title").val().trim()
    let message = $(form + " .message").val().trim()
    let amountOfTime = $(form + " .amount_of_time").val()
    let color = $(form + " .form-color input").val()
    let x, y
    if (form === '#drop-down' && $('#coords__input:checked').val() === 'on') {
        x = $("#coords__x").val().trim()
        y = $("#coords__y").val().trim()
    } else if (form === '#pop-up') {
        x = window.lastClickX
        y = -window.lastClickY
    }

    const bin = {
        title: title,
        message: message,
        amountOfTime: amountOfTime,
        color: color,
        x: x, y: y
    }

    if(ws.connected) {
        ws.createBin(bin)
        closePopup(form)
        clearForm(form)
    } else {
        toastr.error('Server is not responding')
    }
}

const deleteBin = id => {
    if(ws.connected) {
        ws.deleteBin(id)
        closePopup('#pop-up')
    } else {
        toastr.error('Server is not responding')
    }
}

const clearForm = form => {
    $(form + ' .title').val('')
    $(form + " .message").val('')
    $(form + " .amount_of_time").val('INFINITE')
    if (form === '#drop-down') {
        $("#coords__x").val('')
        $("#coords__y").val('')
    }
}

const decodeBins = () => {
    let byteChars = atob(bins)
    let byteArray = new Uint8Array(byteChars.length)
    for (let i = 0; i < byteChars.length; i++) {
        byteArray[i] = byteChars.charCodeAt(i)
    }
    return new TextDecoder().decode(byteArray)
}

const createBinsFromJSON = (json) => {
    let jsonObject = JSON.parse(json)
    jsonObject.forEach(o => createObject(o.id, o.title, o.x, o.y, o.color))
}

let mousePressed = false
let clusterSizeX = window.innerWidth / 7
let clusterSizeY = window.innerWidth / 7

const amountOfCellsX = 100
const amountOfCellsY = 100
field = new Field(amountOfCellsX, amountOfCellsY)
let ws = new WS()
window.clientId = crypto.randomUUID()

const camera = {
    left: 0,
    top: 0,
    width: window.innerWidth,
    height: window.innerHeight
}

const canvas = createCanvas('canvas')
initCanvas()
setFormEvents()
createBinsFromJSON(decodeBins())
ws.connect()

const openPopup = (popup) => {
    $('.' + popup).fadeIn(300)
    $('html').addClass('no-scroll')
}

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
    if(form === '#drop-down') {
        $('#new-bin-checkbox').prop('checked', false)
    } else if (form === '#pop-up') {
        $('.popup-create-bg').fadeOut(200)
        $('.popup-show-bg').fadeOut(200)
        $('html').removeClass('no-scroll')
    }
}

const getAndShowBin = (id) => {
    $.ajax({
        url: '/api/bin',
        method: 'get',
        dataType: 'json',
        data: {id: id},
        success: function (data) {
            showBin(data)
        },
        error: function (data) {
            if(data.code === 'NOT_FOUND') {
                toastr.error('Bin was not found')
            } else if (data.code === 'INTERNAL_SERVER_ERROR') {
                toastr.error('Error while getting the bin')
            } else {
                toastr.error('Server is not responding')
            }
        }
    })
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

$('#coords__input, #coords__auto').change(() => {
    let x = $('#coords__x')
    let y = $('#coords__y')
    if ($('.coords input[type=radio]:checked').attr('id') === 'coords__auto') {
        x.prop('disabled', true)
        y.prop('disabled', true)
        x.removeClass("__req")
        y.removeClass("__req")
        x.removeClass("__error")
        y.removeClass("__error")
    } else {
        x.prop('disabled', false)
        y.prop('disabled', false)
        x.addClass("__req")
        y.addClass("__req")
    }
})

if (urlBin !== null) {
    if (urlBin.statusCode === 'OK') {
        shiftCanvas(-urlBin.body.x * clusterSizeX, urlBin.body.y * clusterSizeY)
        canvas.relativePan(new fabric.Point(0, 0))
        showBin(urlBin.body)
    } else if (urlBin.statusCode === 'NOT_FOUND') {
        toastr.error("Bin was not found")
    }
}

const binToCreate = new URLSearchParams(window.location.search).get('binToCreate')

const checkBinToCreate = () => {
    if(binToCreate !== null && binToCreate !== "") {
        let bin = atob(binToCreate.substring(1, binToCreate.length-1));
        let binObject = JSON.parse(bin)
        ws.createBin(binObject)
    }
}

const copyUrl = (id) => {
    const sysInput = document.createElement('input')
    let url = new URL(location.href)
    url.searchParams.set('id', id)
    sysInput.setAttribute('value', url.toString())
    document.body.appendChild(sysInput)
    sysInput.select()
    document.execCommand('copy')
    document.body.removeChild(sysInput)

    toastr.success("Copied to clipboard")
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

fillTimeOptions()

$('#new-bin-checkbox').change(() => {
    let inputs = $('#new-bin-menu input, #new-bin-menu select, #new-bin-menu textarea, #new-bin-menu button')
    if($('#new-bin-checkbox').is(':checked')) {
        setTimeout(() => inputs.removeAttr('tabindex'), 250 )
    } else {
        inputs.attr('tabindex', '-1')
    }
})

function redirectTo(path, ...params) {
    if(params.length % 2 !== 0) return
    let url = new URL(location.protocol + location.host + path)

    for (let i = 0; i < params.length;) {
        const parName = params[i++]
        const parValue = params[i++]

        if(parName && parValue) {
            url.searchParams.set(parName, parValue)
        }
    }

    document.location.href = url.toString()
}

function adjustUpperMenuWidth() {
    const blank = $('.upper_menu .blank')
    const authorization = $('.upper_menu .authorization')

    blank.width(authorization.width() + 50)
}

adjustUpperMenuWidth()