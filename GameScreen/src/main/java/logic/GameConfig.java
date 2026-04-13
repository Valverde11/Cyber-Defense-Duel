package logic;

public class GameConfig {

    public int initialHp; // Puntos de vida iniciales
    public double baseSpawnRate; // Tasa spawn inicial
    public double baseAttackSpeed; // Velocidad movimiento inicial
    public int scorePerKill; // Puntos por enemigo
    public int difficultyStepScore; // Puntos para nuevo nivel
    public double spawnMultiplierPerLevel; // Factor spawn por nivel
    public double speedAddPerLevel; // Velocidad extra por nivel

    // daño por tipo
    public int damageYellow; // Danio ataque DDoS
    public int damageRed; // Danio ataque Malware
    public int damageBlue; // Danio ataque Credential
}


