const outputPostfix = "output"
let controller // To hold the controller for the readable stream
let assessmentTimeout
let refactoredCodeTimeout
let loaderElement

window.onload = event => {
    loaderElement = document.getElementById("loader")
}

const sendText = event => {
    const inputText = document.getElementById("inputText").value
    const logicModel = document.getElementById("logic-model-select").value
    const qualityModel = document.getElementById("quality-model-select").value
    const securityModel = document.getElementById("security-model-select").value
    const performanceModel = document.getElementById("performance-model-select").value
    const refactorModel = document.getElementById("refactor-model-select").value
    const assessmentOutputElement = document.getElementsByClassName("assessment-output")
    const logicAsessmentOutputElement = document.getElementById(`logic-assessment-${outputPostfix}`)
    const qualityAssessmentOutputElement = document.getElementById(`quality-assessment-${outputPostfix}`)
    const securityAssessmentOutputElement = document.getElementById(`security-assessment-${outputPostfix}`)
    const performanceAssessmentOutputElement = document.getElementById(`performance-assessment-${outputPostfix}`)
    const refactoredCodeElement = document.getElementById("refactored-code")
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

    Promise.all([
        requestReview("logic", logicModel, inputText, logicAsessmentOutputElement, controller),
        requestReview("quality", qualityModel, inputText, qualityAssessmentOutputElement, controller),
        requestReview("performance", performanceModel, inputText, performanceAssessmentOutputElement, controller),
        requestReview("security", securityModel, inputText, securityAssessmentOutputElement, controller),
    ])
    .then(assessments => requestRefactor(refactorModel, inputText, assessments, refactoredCodeElement, controller))
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

const requestReview = async (aspect, model, codeSnippet, outputElement, controller) => {
    return fetch(`/assistant/code/review/${aspect}`, {
        method: "POST",
        signal: controller.signal, // Pass the signal to abort the fetch
        headers: {
            "Accept": "application/x-ndjson",
            "Content-Type": "text/plain",
            "X-Chat-Model": model
        },
        body: codeSnippet
    })
    .then(readNdJsonStream)
    .then(stream => {
        loaderElement.style.display = "none"
        const teed = stream.tee()
        streamContent(teed[0], outputElement, value => value.response)
        return collectAgentResponse(teed[1])
    })
}

const sendReviewRequest = async (aspect, model, codeSnippet, controller) => {
    return fetch(`/assistant/code/review/${aspect}`, {
        method: "POST",
        signal: controller.signal, // Pass the signal to abort the fetch
        headers: {
            "Accept": "application/x-ndjson",
            "Content-Type": "text/plain",
            "X-Chat-Model": model
        },
        body: codeSnippet
    }).then(readNdJsonStream)
}

const requestRefactor = async (model, codeSnippet, assessments, outputElement, controller) => {
    return fetch("/assistant/code/refactor", {
        method: "POST",
        signal: controller.signal,
        headers: {
            "Accept": "application/x-ndjson",
            "Content-Type": "application/json",
            "X-Chat-Model": model
        },
        body: JSON.stringify({
            codeSnippet,
            assessments
        })
    }).then(readNdJsonStream)
    .then(stream => streamContent(stream, outputElement, value => value.refactoredCode))
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

const streamContent = (stream, outputElement, callBackFn = value => value, intervalTime = 5) => {
    const reader = stream.getReader()
    const appendToOutputElement = ({ done, value }) => {
        if (done) {
            outputElement.innerHTML = DOMPurify.sanitize(
                marked.parse(outputElement.innerHTML)
            ).replaceAll("&amp;", "&")
            return
        }
        const responseText = callBackFn(value)
        let index = 0;
        const appendCharacter = () => {
            if (index < responseText.length) {
                outputElement.innerHTML += responseText[index]
                index++
                // Call the function again to append next character
                assessmentTimeout = setTimeout(appendCharacter, intervalTime) // Adjust the interval as needed
            } else {
                // Continue reading the stream after finishing current response
                reader.read().then(appendToOutputElement);
            }
        };
        appendCharacter()
    }
    reader.read().then(appendToOutputElement)
}

const collectAgentResponse = async stream => {
    const reader = stream.getReader()
    let agentResponse
    const collect = ({ done, value }) => {
        if (done) {
            return agentResponse
        }
        if (!agentResponse) {
            agentResponse = value
        }
        agentResponse.response += value.response
        return reader.read().then(collect)
    }
    return reader.read().then(collect)
}

const handleError = (error) => {
    console.error(error)
    loaderElement.style.display = "none"
}
