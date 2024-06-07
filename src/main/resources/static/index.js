const intervalTime = 5
const outputPostfix = "output"
let controller // To hold the controller for the readable stream
let assessmentTimeout
let refactoredCodeTimeout
let loaderElement
let inputTextElement
let refactoredCodeElement
let assessmentOutputElement
let logicAsessmentOutputElement
let qualityAssessmentOutputElement
let securityAssessmentOutputElement
let performanceAssessmentOutputElement

window.onload = event => {
    loaderElement = document.getElementById("loader")
    inputTextElement = document.getElementById("inputText")
    refactoredCodeElement = document.getElementById("refactored-code")
    assessmentOutputElement = document.getElementsByClassName("assesment-output")
    logicAsessmentOutputElement = document.getElementById(`logic-assessment-${outputPostfix}`)
    qualityAssessmentOutputElement = document.getElementById(`quality-assessment-${outputPostfix}`)
    securityAssessmentOutputElement = document.getElementById(`security-assessment-${outputPostfix}`)
    performanceAssessmentOutputElement = document.getElementById(`performance-assessment-${outputPostfix}`)
}

const sendText = event => {
    // Hide loading animation if visible
    loaderElement.style.display = "none"
    // Show loading animation
    loaderElement.style.display = "block"
    // Clear previous output
    for (let index = 0; index < assessmentOutputElement.length; index++) {
        assessmentOutputElement[index].innerHTML = ""
    }
    // Clear previous refactored code
    refactoredCodeElement.innerHTML = ""
    // Clear timeout
    if (assessmentTimeout) {
        clearTimeout(assessmentTimeout)
    }
    if (refactoredCodeTimeout) {
        clearTimeout(refactoredCodeTimeout)
    }
    controller = new AbortController()

    requestAssessment(inputTextElement.value)
    .then(stream => {
        const teed = stream.tee()
        readAssessment(teed[0])
        return combineAssessment(teed[1])
    })
    .then(assessments => requestRefactor(inputTextElement.value, assessments))
    .then(readRefactor)
    .catch(handleError)
}

const stopStreaming = () => {
    loaderElement.style.display = "none"; // Hide the loading animation
    if (controller || !controller.aborted) {
        controller.abort("user canceled"); // Abort the fetch request
    }
    if (assessmentTimeout) {
        clearTimeout(assessmentTimeout)
    }
    if (refactoredCodeTimeout) {
        clearTimeout(refactoredCodeTimeout)
    }
}

const requestAssessment = async codeSnippet => {
    return fetch("/stream/code/review/stage/0", {
        method: "POST",
        signal: controller.signal, // Pass the signal to abort the fetch
        headers: {
            "Accept": "application/x-ndjson",
            "Content-Type": "text/plain"
        },
        body: codeSnippet
    }).then(readNdJsonStream)
}

const requestRefactor = async (codeSnippet, assessments) => {
    return fetch("/stream/code/review/stage/1", {
        method: "POST",
        signal: controller.signal,
        headers: {
            "Accept": "application/x-ndjson",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            codeSnippet,
            assessments
        })
    }).then(readNdJsonStream)
}

const readNdJsonStream = response => {
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    const process = (controller, { done, value }) => {
        if (done) {
            controller.close()
            return;
        }
        const text = decoder.decode(value, {
            stream: true
        })
        const payloads = text.split("\n")
        for (const jsonString of payloads) {
            const trimmed = jsonString.trim()
            if (trimmed.length > 0) {
                const json = JSON.parse(trimmed)
                controller.enqueue(json)
            }
        }
        return reader.read().then(result => process(controller, result))
    }
    return new ReadableStream({
        start: async controller => reader.read().then(result => process(controller, result))
    })
}

const readAssessment = stream => {
    const reader = stream.getReader()
    loaderElement.style.display = "none"
    const display = ({ done, value }) => {
        if (done) {
            for (let index = 0; index < assessmentOutputElement.length; index++) {
                assessmentOutputElement[index].innerHTML = marked.parse(assessmentOutputElement[index].innerHTML)
            }
            return
        }
        const responseText = value.response;
        let index = 0;
        // Function to append one character at a time
        let outputElement
        const appendCharacter = () => {
            if (index < responseText.length && (outputElement = getAssesmentOutputElement(value.agentType))) {
                outputElement.innerHTML += responseText[index]
                index++
                // Call the function again to append next character
                assessmentTimeout = setTimeout(appendCharacter, intervalTime) // Adjust the interval as needed
            } else {
                // Continue reading the stream after finishing current response
                reader.read().then(display);
            }
        };
        appendCharacter()
    }
    reader.read().then(display)
}

const getAssesmentOutputElement = agentType => {
    switch (agentType) {
        case "LOGIC_ASSESSMENT": return logicAsessmentOutputElement
        case "QUALITY_ASSESSMENT": return qualityAssessmentOutputElement
        case "SECURITY_ASSESSMENT": return securityAssessmentOutputElement
        case "PERFORMANCE_ASSESSMENT": return performanceAssessmentOutputElement
    }
}

const combineAssessment = async stream => {
    const reader = stream.getReader()
    const combinedAssessment = {}
    const combine = ({ done, value }) => {
        if (done) {
            return Object.values(combinedAssessment)
        }
        const agentType = value.agentType
        if (!combinedAssessment[agentType]) {
            combinedAssessment[agentType] = { response: "", agentType }
        }
        combinedAssessment[agentType].response += value.response
        return reader.read().then(combine)
    }
    return reader.read().then(combine)
}

const readRefactor = stream => {
    const reader = stream.getReader()
    const display = ({ done, value }) => {
        if (done) {
            refactoredCodeElement.innerHTML = DOMPurify.sanitize(marked.parse(refactoredCodeElement.innerHTML))
                .replaceAll("&amp;", "&")
            return
        }
        const refactoredCode = value.refactoredCode
        let index = 0
        const appendCharacter = () => {
            if (index < refactoredCode.length) {
                refactoredCodeElement.innerHTML += refactoredCode[index]
                index++
                // Call the function again to append next character
                refactoredCodeTimeout = setTimeout(appendCharacter, intervalTime) // Adjust the interval as needed
            } else {
                // Continue reading the stream after finishing current response
                reader.read().then(display)
            }
        }
        appendCharacter()
    }
    reader.read().then(display)
}

const handleError = (error) => {
    console.error(error)
    loaderElement.style.display = "none"
}
