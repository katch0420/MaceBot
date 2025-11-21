# MaceBot

A Minecraft Fabric mod that adds an AI-powered player bot specialized in mace combat with advanced automation features.

## 🤖 About

MaceBot introduces an intelligent player bot to your Minecraft world that can autonomously navigate, fight, and manage inventory. The bot is specifically designed for mace combat scenarios and includes support for totems, elytra flight, and automatic equipment management.

## ✨ Features

- **🎯 AI Player Bot**: Intelligent bot that can navigate and interact with the world
- **⚔️ Mace Combat Specialization**: Optimized for mace weapon combat with totem support
- **📦 Smart Inventory Management**: Automatic inventory refilling and kit distribution
- **🪶 Elytra Integration**: Enhanced mobility with elytra flight capabilities
- **💎 Multi-Material Support**: Compatible with both diamond and netherite equipment sets
- **🎮 Comprehensive Commands**: Full command system for bot control and configuration

## 📋 Requirements

- Minecraft (Fabric compatible version)
- [Fabric Loader](https://fabricmc.net/use/)
- [Fabric API](https://modrinth.com/mod/fabric-api)

## 🚀 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for your Minecraft version
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in your `mods` folder
3. Download the latest MaceBot release
4. Place the MaceBot `.jar` file in your `mods` folder
5. Launch Minecraft with the Fabric profile

## 🎮 Commands

### Bot Management

/macebot bot spawn              # Spawn the bot at your location
/macebot bot pause              # Pause bot AI activity
/macebot bot play               # Resume bot AI activity

### Bot Configuration

/macebot bot settings auto-refill <true/false>    # Toggle auto inventory refill
/macebot bot settings elytra <true/false>         # Toggle elytra usage
/macebot bot mace-kit <true/false>                # Equip mace kit (true=netherite, false=diamond)

### Player Commands

/macebot player mace-kit <true/false>             # Give yourself a mace kit
/macebot player settings auto-refill <true/false> # Toggle your auto-refill setting

## 🔧 Configuration

The bot can be configured through in-game commands to customize:
- Automatic inventory management
- Equipment material preferences (diamond vs netherite)
- Elytra flight behavior
- Combat AI responsiveness

## 🏗️ Development

### Technical Stack
- **Framework**: Fabric Modding API
- **Language**: Java
- **Build System**: Gradle
- **Mod ID**: `macebot`

### Key Components
- Custom player bot entity with advanced AI
- Brigadier-based command system
- Mixin integrations for enhanced functionality
- Custom networking for bot communication
- Inventory management utilities
- Debug and tracing systems

### Building from Source
```bash
git clone https://github.com/katch0420/MaceBot.git
cd MaceBot
./gradlew build

📝 Changelog
v1.0.0 - Initial Release

✅ Core bot spawning and AI functionality
✅ Mace combat specialization with totem support
✅ Comprehensive inventory management system
✅ Elytra flight integration
✅ Multi-material equipment support (diamond/netherite)
✅ Full command system implementation
✅ Automatic refilling capabilities
✅ Player kit management
✅ Configurable bot settings

📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.
📞 Support
For support, bug reports, or feature requests, please open an issue on the GitHub repository.

Made with ❤️ for the Minecraft community
