# COMP30022 IT Project Sem2 2018 - HomeCare
Team Portugal: Ckyever Gaviola 756550, Pui Yam Yeung 743270, Brendan Leung 833995, Sayyaf Waseem 841546, Wenzhen Yue 752632

## Project Overview
### Background
For our project, Team Portugal will be creating an Android application designed to make location tracking and navigation for assisted people to experience a significantly easier lifestyle change. By creating a networked relationship between caregivers and the assisted people using our application, we want to provide easy tracking functionality for caregivers and easy communication and navigation for the assisted. With these functionalities, the assisted people will not require supervision or someone to accompany them every place they go. The application will allow for text and voice chat, amongst other functionality, including the ability to send an emergency notification to all of their caregivers.

### Target Users
Assisted person:
- Novice smartphone user
- Age varies, but mostly 60 years old plus
- May be visually impaired
- May have movement disabilities (using walking stick/wheelchair, which is hard to control the phone by both hands) or other physical disabilities
- May have hearing loss
- May have speech disorder (e.g. dysarthria, hard to pronounce words)

Caregiver:
- Familiar with basic smartphone functionality
- No functional disabilities (move easily, speak easily)
- Age varies but mostly much more younger
- May have knowledge about assisted person’s destination
- Concern about assisted person’s current locations or wellbeing
- Could be assisted person’s trusted friend/relative (e.g. family members, neighbours, friends)

### Product
The end product of this project will be a smartphone application running on the android platform. In order to reach the largest audience the target version will be Nougat or later since it boasts the largest user distribution according to the developer dashboard.

The application will enable an assisted person to reach their primary caregiver via text or voice chat and allow them to receive assistance with navigating the interface through written or verbal instructions. Meanwhile the assigned caregiver will be able to view their whereabouts via location tracking services for peace of mind. At any time the assisted user can initiate an emergency protocol, with a press of a button, flooding their listed carers with a request for immediate assistance in the form of a notification. Since the overall theme of the app is to help overcome the difficulties that assisted people often have with technology, all these features will be integrated into a user-friendly interface.

## Testing
Testing modules are located in the master branch under `Portugal_Homecare/app/src/androidTest` and `Portugal_Homecare/app/src/test`

## Build Instructions
1. Download repository to local machine
2. Open repository in AndroidStudio
3. Add Sinch API keys to the constants `APP_KEY` and `APP_SECRET` at the top of the `Portugal_Homecare/app/src/main/java/com/example/sayyaf/homecare/communication/SinchService.java` class
4. Add Google Map API key to the string value in `Portugal_Homecare/app/src/debug/res/values/google_maps_api.xml`
5. Select `Build` then `Make Project`

Note: API keys have been emailed to william.v@unimelb.edu.au with subject line "Team Portugal API Keys". Ensure device selected to run the app operates on Android API 24 or higher.
