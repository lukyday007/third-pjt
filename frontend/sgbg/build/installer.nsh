!macro customInstall
  WriteRegStr HKCR "sgbg" "" "URL:sgbg Protocol"
  WriteRegStr HKCR "sgbg" "URL Protocol" ""
  WriteRegStr HKCR "sgbg\shell" "" ""
  WriteRegStr HKCR "sgbg\shell\open" "" ""
  WriteRegStr HKCR "sgbg\shell\open\command" "" '"$INSTDIR\Sgbg.exe" "%1"'
!macroend

!macro customUnInstall
  DeleteRegKey HKCR "sgbg"
!macroend
