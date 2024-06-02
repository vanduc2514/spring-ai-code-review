class Queue {
	constructor() {
		this.items = {}
		this.frontIndex = 0
		this.backIndex = 0
	}
	enqueue(item) {
		this.items[this.backIndex] = item
		this.backIndex++
		return item + ' inserted'
	}
	dequeue() {
		if (Object.getOwnPropertyNames(this.items).length !== 0) {
			const item = this.items[this.frontIndex]
			delete this.items[this.frontIndex]
			this.frontIndex++
			return item;
		} else {
			return undefined;
		}
	}
	peek() {
		return this.items[this.frontIndex]
	}
	clear() {
		this.items = {}
		this.frontIndex = 0
		this.backIndex = 0
	}
	get printQueue() {
		return this.items;
	}
}

const queue = new Queue();

const URL = '/stream/code/review/stage/0';

const agentTypeLabel = {
	QUALITY_ASSESSMENT: 'QUALITY ASSESSMENT: \n',
	SECURE_ASSESSMENT: 'SECURE ASSESSMENT: \n',
	LOGIC_ASSESSMENT: 'LOGIC ASSESSMENT: \n'
};
const intervalTime = 10;

let controller; // To hold the controller for the readable stream
let intervalId; // To hold the ID of the interval

function sendText() {
	let count = 1;
	const inputText = document.getElementById('inputText').value;
	const outputElement = document.getElementById('output');
	const loader = document.getElementById('loader');

	//Clear queue
	queue.clear();

	// Clear previous output
	outputElement.innerHTML = '';

	// Hide loading animation if visible
	loader.style.display = 'none';

	// Create new controller
	controller = new AbortController();

	// Show loading animation
	loader.style.display = 'block';
	let agentTypePrintState = {
	}
	fetch(URL, {
		method: 'POST',
		signal: controller.signal, // Pass the signal to abort the fetch
		headers: {
			"Accept": "text/event-stream",
			"Content-Type": "text/plain"
		},
		body: inputText
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

						let text = new TextDecoder("utf-8").decode(value);
						var arr = text.split("\n\ndata:");
						for (let i in arr) {
							let item = arr[i];
							var jsonObject = toJsonObject(item);
							let response = jsonObject["response"];
							let agentType = jsonObject["agentType"];
							if (typeof response !== 'undefined') {
								if (agentTypePrintState[agentType] == 0 || typeof agentTypePrintState[agentType] === 'undefined') {
									let prefix = count == 1 ? count + "." : "\n" + count + ".";
									queue.enqueue(prefix + getLabel(agentType));
									count++;
								}
								queue.enqueue(response);
								if (typeof intervalId == 'undefined') {
									setUpInterval();
								}
							}
						}
						controller.enqueue(value);
						push();
					});
				}
				push();

				function setUpInterval() {
					let i = 0;
					let item = queue.dequeue()
					intervalId = setInterval(() => {
						if (typeof item !== 'undefined' && i < item.length) {
							// Once the first character is displayed, hide the loader
                            loader.style.display = 'none';
                            outputElement.textContent += item.charAt(i);
                            output.style.height = `${output.scrollHeight}px`;
							i++;
						} else {
							item = queue.dequeue();
							i = 0;
						}
					}, intervalTime);
				}

				function toJsonObject(item) {
					if (!item.startsWith("{")) {
						let index = item.indexOf("{");
						if (index != -1) {
							item = item.substring(index);
						} else {
							item = "";
						}
					}
					var jsonObject = {};
					if (item !== "") {
						try {
							jsonObject = JSON.parse(item);
							console.log(jsonObject);
						} catch (err) {
							console.log(err);
						}
					}
					return jsonObject;
				}

				function getLabel(agentType) {
					let value = agentTypePrintState[agentType];
					if (typeof value == "undefined") {
						value = 0;
						agentTypePrintState[agentType] = value;
					}
					let label = agentTypeLabel[agentType];
					let finaleLabel = label == null ? agentType + ": \n" : label;
					agentTypePrintState[agentType] = ++value;

					return finaleLabel;
				}
			}
		})
	}).catch(error => {
		console.error('Error:', error);
		// Hide loading animation on error
		loader.style.display = 'none';
	});
}

function stop() {
	clearInterval(intervalId)
}

function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}

function stopStreaming() {
	if (controller) {
		controller.abort(); // Abort the fetch request
		const loader = document.getElementById('loader');
		loader.style.display = 'none'; // Hide the loading animation
		queue.clear();
	}
}