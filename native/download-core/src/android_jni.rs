//! Minimal JNI surface for Android integration.
use jni::objects::JClass;
use jni::sys::jstring;
use jni::JNIEnv;

#[no_mangle]
pub extern "system" fn Java_com_devroom_download_NativeBridge_nativeHealthCheck(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    env.new_string(crate::health_check())
        .expect("JNI string allocation failed")
        .into_raw()
}
