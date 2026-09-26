# Runs on the phone (via adb shell). Adds AutoSkip to the enabled accessibility
# services without removing any service the user already has on.
# Right after a fresh install Android may reset the setting, so verify and retry.
svc=com.autoskip.app/com.autoskip.app.AutoSkipService
for attempt in 1 2 3 4 5; do
  cur=$(settings get secure enabled_accessibility_services)
  case "$cur" in
    *"$svc"*) break ;;
    null|"") settings put secure enabled_accessibility_services "$svc" ;;
    *) settings put secure enabled_accessibility_services "$cur:$svc" ;;
  esac
  settings put secure accessibility_enabled 1
  sleep 2
done
echo "Servicios activos: $(settings get secure enabled_accessibility_services)"
