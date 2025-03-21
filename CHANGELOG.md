## 0.6.2
> Published 21 Mar 2025
- support Grok (https://x.ai) API

## 0.6.1
> Published 18 Mar 2025
- actualized list of supported models

## 0.6.0
> Published 26 Feb 2025
- support DeepSeek platform

## 0.5.4
> Published 31 Jan 2025
- update to ktor 3.0.3
- publish ios version

## 0.5.3
> Published 20 Nov 2024
- Fixes
  - Use max_tokens instead of unsupported max_completion_tokens parameter for Azure OpenAi requests.
  - Change order of messages to have system instructions always the first (some providers are sensitive about this)

## 0.5.2
> Published 19 Nov 2024
- support Azure OpenAI service
- fix minor errors

## 0.5.1
- Add default value for maximum tokens parameter in Anthropic AI request

## 0.5.0
> Published 18 Nov 2024
- support `responseFormat` attribute for text generation requests 

## 0.4.4
> Published 14 Nov 2024
- add finish reason to SambaNova response
- fix errors:
    - wrong usage of system instructions for Google client
    - typo in usage response parameter names
    - wrong processing of usage values for SambaNova response

## 0.4.3
- add support for Cerebras Inference

## 0.4.2
> Published 11 Nov 2024
- add support for Together AI

## 0.4.1
- add support for Anthropic Cloud

## 0.4.0
- add support for SambaNova Cloud

## 0.3.1
> Published 26 Oct 2024
- update ktor to 3.0

## 0.3.0
> Published 23 Oct 2024
- added setting custom http timeout for created client

## 0.2.1
> Published 09 Oct 2024
- Fix errors with empty system instructions

## 0.2.0
- added support for AWS Bedrock
- added wrapper to call from Java code
