The AWS SDK lacks supportive documentation onto initializing AWS SDK resources with developer provided identity IDs.

As a workround, in my use case, I've managed to initialize the SDK by initializing the SDK and passing on some of the Managers (Cognito Identity Manager, Developer Identity Provider and Federation).
