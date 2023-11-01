
const extractData = (form) => {
    form = '#' + form
    if(!validateForm(form)) {
        return
    }

    let title = $(form + " .title").val()
    let message = $(form + " .message").val()
    let amountOfTime = $(form + " .amount_of_time").val()
    let x, y
    if (form === '#drop-down' && $('#coords__input:checked').val() === 'on') {
        x = $("#coords__x").val()
        y = $("#coords__y").val()
    } else if (form === '#pop-up') {
        x = window.lastClickX
        y = window.lastClickY
    }

    const bin = {
        title: title,
        message: message,
        amountOfTime: amountOfTime,
        x: x, y: y
    }

    ws.sendMessage(bin)
    closePopup()
}

const validateForm = (form) => {
    let errorCounter = 0
    let formReq = $(form + ' .__req')

    for (let i = 0; i < formReq.length; i++) {
        const input = formReq[i];
        formRemoveError(input)

        if(input.value.length === 0) {
            formAddError(input)
            errorCounter++
        }

        input.classList.forEach(a => {
            if(a.includes('__max-length') && !isLengthValid(input)) {
                formAddError(input)
                errorCounter++
            }
        })

        input.classList.forEach(a => {
            if(a.includes('__max-value') && !isValueValid(input)) {
                formAddError(input)
                errorCounter++
            }
        })
    }

    return errorCounter === 0
}

const formAddError = (input) => {
    input.classList.add('__error')
}

const formRemoveError = (input) => {
    input.classList.remove('__error')
}

const setFormEvents = () => {
    $("form").on('submit', function (e) {
        e.preventDefault()
    })
}

const isLengthValid = input => {
    let el = input.getAttribute('class').split(' ').filter(e => e.startsWith('__max-length')).toString()
    return (input.value.length <= parseInt(el.match(/\d+$/).toString()));
}

const isValueValid = (input) => {
    let minVal = parseInt(input.getAttribute('class').split(' ')
        .filter(e => e.startsWith('__min-value')).toString().match(/[\d-]+$/).toString())
    let maxVal = parseInt(input.getAttribute('class').split(' ')
        .filter(e => e.startsWith('__max-value')).toString().match(/[\d-]+$/).toString())
    return minVal <= input.value && input.value <= maxVal
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
    jsonObject.forEach(a => createObject(a.id, a.title, a.x, a.y))
}

let mousePressed = false
let clusterSizeX = Math.floor(window.innerWidth / 7)
let clusterSizeY = Math.floor(window.innerWidth / 7)
let ws = new WS()

const canvas = createCanvas('canvas')
/*const minimap = new fabric.Canvas('minimap', {containerClass: 'minimap', selection: false})*/
initCanvas()
setFormEvents()
createBinsFromJSON(decodeBins())
ws.connect()

const openPopup = (popup) => {
    $('.' + popup).fadeIn(300)
    $('html').addClass('no-scroll')
}

$('.close-popup').click(() => {
    closePopup()
})

$('.popup-create-bg').click(e => {
    if (e.target.className === 'popup-create-bg' && confirm('Are you sure?')) {
        closePopup()
    }
})

$('.popup-show-bg').click(e => {
    if (e.target.className === 'popup-create-bg') {
        closePopup()
    }
})


const closePopup = () => {
    $('.popup-create-bg').fadeOut(200)
    $('.popup-show-bg').fadeOut(200)
    $('html').removeClass('no-scroll')
}

const getAndShowBin = (id) => {
    $.ajax({
        url: '/map/bin',
        method: 'get',
        dataType: 'json',
        data: {id: id},
        success: function (data) {
            if (data.code === 0) {
                showBin(data)
            } else if (data.code === 1) {
                toastr.error("Error while getting the bin")
            }
        }
    })
}

const showBin = (data) => {
    $('.show__title').text(data.title)
    $('.show__options').text(`x:${data.x} y:${data.y}`)
    $('.show__id').text(data.id)
    $('.show__message').text(data.message)

    openPopup('popup-show-bg')
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
    if (urlBin.code === 0) {
        shiftCanvas(-urlBin.x * clusterSizeX, -urlBin.y * clusterSizeY)
        showBin(urlBin)
    } else if (urlBin.code === 1) {
        toastr.error("Bin was not found")
    }
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