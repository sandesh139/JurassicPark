// Asset is a wrapper for the various asset types that
// will need to be passed from the back-end to the front-end.
//
// Internally the Asset contains an AssetType and an Object.
// The AssetType is used to cast the Object back to it's concrete type.
public class Asset {
    private AssetType type;
    private Object asset;

    public Asset(AssetType type, Object asset) {
        this.type = type;
        this.asset = asset;
    }

    public AssetType getType() {
        return type;
    }

    public Object getAsset() {
        return asset;
    }
}
