# sms-interceptor
This is a simple, lifecycle aware, sms interceptor for Android apps.

It provides:
* A simple callback to handle received SMSs
* No need to register/unregister BroadcastReceivers
* Filtering messages based on recipient's number or message body


[ ![Download](https://api.bintray.com/packages/xabaras/maven/SmsInterceptor/images/download.svg) ](https://bintray.com/xabaras/maven/SmsInterceptor/_latestVersion)
 
## How do I get set up? ##
Get it via Gradle
```groovy
implementation 'it.xabaras.android:sms-interceptor:1.0'
```
or Maven
```xml
<dependency>
  <groupId>it.xabaras.android</groupId>
  <artifactId>sms-interceptor</artifactId>
  <version>1.0</version>
  <type>pom</type>
</dependency>
```

Or download the [latest AAR](https://bintray.com/xabaras/maven/SmsInterceptor/_latestVersion) and add it to your project's libraries.

## Usage ##
Here is a non-comprehensive guide to SmsInterceptor for any further information you can reference the library sources and/or the sample app sources.

In order to start listening for incaming SMSs you just neet to create a new SmsInterceptor instance
```kotlin
val smsInterceptor: SmsInterceptor = SmsInterceptor(context)
```

and call the *startListening* method passing in a callback to be invoked when a new SMS is received
```kotlin
smsInterceptor.startListening { fromNumber, message ->
    // Do what you want with fromNumber and message body
}
```
N.B. startListening requires [Manifest.permission.RECEIVE_SMS](https://developer.android.com/reference/android/Manifest.permission.html#RECEIVE_SMS), so you will need to declare it in the [AndroidManifest.xml](https://developer.android.com/guide/topics/manifest/manifest-intro) and properly [handle it at runtime](https://developer.android.com/training/permissions/requesting) when needed.

### Lifecycle ###
_SmsInterceptor_  is based upon [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/) therefore is capable of handling lifecycle itself.

This means that _SmsInterceptor_ will stop listening for SMSs when your Activity or Fragment is paused and resume listening when the Fragment/Activity is restored.

So you won't need to do things like these:
```kotlin
fun onPause() {
    super.onPause()
    smsInterceptor.stopListening()
}

fun onResume() {
    super.onResume()
    smsInterceptor.resumeListening()
}
```
_SmsInterceptor_ will handle app lifecycle for you the right way.
All you have to do is passing in a lifecycle when you create the object
```kotlin
val smsInterceptor: SmsInterceptor = SmsInterceptor(this, lifecycle)
```
 
 Clearly you can always stop listening for incoming messages by calling
```kotlin
smsInterceptor.stopListening()
```
 
### Filtering ###
_SmsInterceptor_ can be configured to filter messages based on the recipient's phone number or the message body.
 
#### Filtering by phone number ####
You can specify one or more phone numbers to filter the message on
```kotlin
smsInterceptor.setNumberFilter("+3912345678")
```

```kotlin
smsInterceptor.setNumberFilter("+3912345678", "+441235678")
```
 
#### Filtering by message body ####
You can set a filter on message body by specifying a Regular Expression
```kotlin
smsInterceptor.setBodyFilter("(\d+)")
```

or by passing in a lambda ((messageBody: String) -> Boolean) taking a String as input and returning a Boolean value 
```kotlin
smsInterceptor.setBodyFilter {
    it.startsWith("Hello")
}
```
