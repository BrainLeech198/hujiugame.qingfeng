; 氢风 (qingfeng) Windows Installer
; 构建前需要先运行 package.bat 构建 launcher，并编译 JAR

#define MyAppName "氢风"
#define MyAppVersion "1.0.0-beta"
#define MyAppPublisher "HujiuGame"
#define MyAppURL "https://brainleech198.github.io/hujiugame.qingfeng/"
#define MyAppExeName "launcher.exe"
#define MyAppAssocName "QF Game File"
#define MyAppAssocExt ".qfg"
#define MyAppAssocKey StringChange(MyAppAssocName, " ", "") + MyAppAssocExt

[Setup]
AppId={{CCD1AB7D-528E-4681-A79E-49183550CABA}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf}\qingfeng
UninstallDisplayIcon={app}\{#MyAppExeName}
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
ChangesAssociations=yes
DisableProgramGroupPage=yes
PrivilegesRequired=admin
OutputDir=.\dist
OutputBaseFilename=qingfeng_setup_windows
SetupIconFile=.\setup.ico
SolidCompression=yes
Compression=lzma2/ultra64
LZMAUseSeparateProcess=yes
WizardStyle=modern
LicenseFile=.\LICENSE

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "dist\launcher\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\launcher\api-ms-win-core-path-l1-1-0.dll"; DestDir: "{app}"; Flags: ignoreversion
; Win7 将此 DLL 视为 KnownDLL，仅从 System32 加载，故需额外安装到系统目录
Source: "dist\launcher\api-ms-win-core-path-l1-1-0.dll"; DestDir: "{sys}"; Flags: ignoreversion; Check: IsWin64
Source: "dist\launcher\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: ".\icon.ico"; DestDir: "{app}"; Flags: ignoreversion

[Registry]
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocExt}\OpenWithProgids"; ValueType: string; ValueName: "{#MyAppAssocKey}"; ValueData: ""; Flags: uninsdeletevalue
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}"; ValueType: string; ValueName: ""; ValueData: "{#MyAppAssocName}"; Flags: uninsdeletekey
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\icon.ico"
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#MyAppExeName}"" ""%1"""

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\icon.ico"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\icon.ico"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

