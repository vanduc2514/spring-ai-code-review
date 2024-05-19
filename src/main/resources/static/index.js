const URL = '/stream/code/review/stage/0';
    const agentType = {
        quantity: 'QUNATITY_ASSESSMENT',
        secure: 'SECURE_ASSESSMENT',
        logic: 'LOGIC_ASSESSMENT'
    };
    const intervalTime = 30;

    let controller; // To hold the controller for the readable stream
    let intervalId; // To hold the ID of the interval

    function sendText() {
        const inputText = document.getElementById('inputText').value;
        const outputElement = document.getElementById('output');
        const loader = document.getElementById('loader');

        // Clear previous output
        outputElement.innerHTML = '';

        // Hide loading animation if visible
        loader.style.display = 'none';

        // Clear previous interval if exists
        clearInterval(intervalId);

        // Create new controller
        controller = new AbortController();

        // Show loading animation
        loader.style.display = 'block';

        fetch(URL, {
            method: 'POST',
            signal: controller.signal, // Pass the signal to abort the fetch
            headers: {
                "Content-Type": "text/plain"
            },
            body: inputText
        }).then(function(response) {
            return response;
        }).then(response => {
            const reader = response.body.getReader();
            return new ReadableStream({
                start(controller) {
                    function push() {
                        reader.read().then(({ done, value }) => {
                            if (done) {
                                controller.close();
                                return;
                            }
                            const jsonValue =  JSON.parse(new TextDecoder().decode(value));
                            const text =  jsonValue.get(0)["response"] //Need to check when integrate BE
                            const agentType = jsonValue.get(0)["agentType"]
                            let i = 0;
                            intervalId = setInterval(() => {
                                if (i < text.length) {
                                    // Once the first character is displayed, hide the loader
                                    loader.style.display = 'none';
                                    outputElement.textContent += text.charAt(i);
                                    i++;
                                } else {
                                    clearInterval(intervalId);
                                }
                            }, intervalTime); // Adjust speed as necessary
                            controller.enqueue(value);
                            push();
                        });
                    }
                    push();
                }
            })
        })
        .catch(error => {
            console.error('Error:', error);
            // Hide loading animation on error
            loader.style.display = 'none';
        });
    }

    function stopStreaming() {
        if (controller) {
            controller.abort(); // Abort the fetch request
            const loader = document.getElementById('loader');
            loader.style.display = 'none'; // Hide the loading animation
            clearInterval(intervalId); // Clear the interval
        }
    }