# Moment
[![Circle CI](https://circleci.com/gh/AndrewJack/moment-for-android-wear.svg?style=svg)](https://circleci.com/gh/AndrewJack/moment-for-android-wear)

Moment is a simple drawing messenger for Android Wear. Moment (originally called DrawChat) was created at the Droidcon London 2014 hackathon

##Features
- Draw a picture directly on your smart watch.
- And then send it straight to a friend, without getting your phone out once!
- View all your received and sent drawing on your phone
- Easy sign in with Google+

##How to Use
- Install Moment on your phone
- The wear app will automatically be installed when it is connected
- Sign in on your phone
- Add or invite friends
- Start drawing!

##Debugging over bluetooth

```
adb forward tcp:4444 localabstract:/adb-hub
adb connect localhost:4444
```

## Delete App from wear
```
adb -s localhost:4444 shell pm uninstall -k technology.mainthread.apps.moment.dev.debug
```

## Pair emulator to phone

```
adb -d forward tcp:5601 tcp:5601
```

License
-------

    Copyright 2015 Andrew Jack

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
