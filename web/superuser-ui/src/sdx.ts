type SdxData = Record<string, any>;

export const sdx: {
  d: SdxData;
  setData: (data: SdxData) => void;
  changeState?: () => void;
} = {
  d: {},

  setData: function (data: SdxData) {
    Object.keys(data).forEach((k) => {
      this.d[k] = data[k];
    });

    if (this.changeState) {
      this.changeState();
    }
  },
};