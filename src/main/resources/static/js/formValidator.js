const validateForm = (form) => {
    let errorCounter = 0
    let formReq = $(form + ' .__req')

    for (let i = 0; i < formReq.length; i++) {
        const input = formReq[i];
        formRemoveError(input)

        if(input.value.trim().length === 0) {
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