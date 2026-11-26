//! DEVROOM DOWNLOAD native core foundation.
pub const CORE_VERSION: &str = env!("CARGO_PKG_VERSION");

pub fn health_check() -> &'static str {
    "devroom-download-core:ok"
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn reports_core_health() {
        assert_eq!(health_check(), "devroom-download-core:ok");
    }

    #[test]
    fn exposes_package_version() {
        assert_eq!(CORE_VERSION, "0.1.0");
    }
}
