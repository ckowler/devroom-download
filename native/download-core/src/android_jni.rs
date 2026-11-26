//! Minimal JNI surface for Android integration.
use jni::objects::{JClass, JString};
use jni::sys::{jint, jstring};
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

/// Blocking native transfer entry point. Kotlin must call this from a worker
/// thread; Android lifecycle and durable scheduling remain Kotlin-owned.
#[no_mangle]
pub extern "system" fn Java_com_devroom_download_NativeBridge_nativeDownload(
    mut env: JNIEnv,
    _class: JClass,
    url: JString,
    destination: JString,
    connections: jint,
) -> jstring {
    let result = (|| -> Result<String, String> {
        let url: String = env
            .get_string(&url)
            .map_err(|error| error.to_string())?
            .into();
        let destination: String = env
            .get_string(&destination)
            .map_err(|error| error.to_string())?
            .into();
        let report = crate::download_to_path(
            &url,
            destination,
            connections.max(1) as usize,
        )
        .map_err(|error| error.to_string())?;
        Ok(format!(
            "OK|{}|{}|{}",
            report.bytes_written, report.used_parallel_ranges, report.connections
        ))
    })();

    let message = result.unwrap_or_else(|error| format!("ERROR|{error}"));
    env.new_string(message)
        .map(|value| value.into_raw())
        .unwrap_or(std::ptr::null_mut())
}
