// src/util/util.ts
import moment from 'moment';
import { Modal } from 'antd';
import {jwtDecode} from "jwt-decode";
import { message } from "antd";

const TOKEN_KEY = "jwt_token";

declare global {
  interface Window {
    $: any; // tell TypeScript that $ exists
  }
}

const $ = window.$;

type OrientationType = 'portrait' | 'landscape';

class Util {

   /** ---------------- TOKEN HELPERS ---------------- */
  getToken = (): string | null => {
    return localStorage.getItem(TOKEN_KEY);
  };
  setToken = (token: string): void => localStorage.setItem(TOKEN_KEY, token);
  clearToken = (): void => localStorage.removeItem(TOKEN_KEY);

  /** JWT Decoding */
  decodeToken = <T = any>(token?: string): T | null => {
    try {
      return jwtDecode<T>(token ?? this.getToken() ?? "");
    } catch {
      return null;
    }
  };

  /** Check if token expired */
  isTokenExpired = (token?: string): boolean => {
    const decoded: any = this.decodeToken(token);
    if (!decoded?.exp) return true;
    return Date.now() >= decoded.exp * 1000;
  };


 /** Axios Error Handler */
  handleAxiosError = (error: any): void => {
    if (!error.response) {
      message.error("Network error. Please check your connection.");
      return;
    }

    const status = error.response.status;
    if (status === 401) {
      this.clearToken();
      message.warning("Session expired. Please log in again.");
    } else if (status === 403) {
      message.error("Access denied.");
    } else if (status >= 500) {
      message.error("Server error. Please try again later.");
    } else if (error.response.data?.message) {
      message.error(error.response.data.message);
    }
  };

  /** Array helpers */
  arr = <T = any>(arg: T[] | any): T[] => (Array.isArray(arg) ? arg : []);
  obj = (arg: object | any): object => (typeof arg !== 'object' || arg === null ? {} : arg);

  /** Timezone helpers */
  getTimezoneOffset = (): number => new Date().getTimezoneOffset();

  getTimeZone = (): string => {
    let n = this.getTimezoneOffset();
    const sign = n >= 0 ? '-' : '+';
    n = Math.abs(n);
    const h = Math.floor(n / 60);
    const m = n % 60;
    return `${sign}${h}:${m}`;
  };

  /** CSV helpers */
  convertToCSVBlob = (objArray: any[], header: string[]): Blob => {
    const array = typeof objArray !== 'object' ? JSON.parse(objArray) : objArray;
    let str = '';
    let line = '';

    for (const index in header) {
      if (line !== '') line += ',';
      line += header[index];
    }
    str += line + '\r\n';

    array.forEach((item :Record<string, any>) => {
      line = '';
      for (const index in header) {
        if (line !== '') line += ',';
        line += item[index] ? `"${item[index]}"` : ' ';
      }
      str += line + '\r\n';
    });

    return new Blob([str], { type: 'text/csv;charset=utf-8' });
  };

  /** Object helpers */
  copyObj = <T>(obj: T): T => JSON.parse(JSON.stringify(obj));

  /** File extension helpers */
  getExt = (filename: string): string => {
    const ext = /[^.]+$/.exec(filename);
    return ext ? ext[0].toLowerCase() : '';
  };

  checkImageExt = (filename: string): boolean => ['jpg', 'jpeg', 'png'].includes(this.getExt(filename));
  checkPdfExt = (filename: string): boolean => ['pdf'].includes(this.getExt(filename));
  checkVideoExt = (filename: string): boolean => ['mp4', 'webp', 'mkv', 'mov'].includes(this.getExt(filename));

  /** File validation */
  checkImage = (ob: any, size_mb = 2): boolean => this.validateFile(ob, this.checkImageExt, size_mb, 'Image', ['jpg', 'png']);
  checkPdf = (ob: any, size_mb = 2): boolean => this.validateFile(ob, this.checkPdfExt, size_mb, 'Pdf', ['pdf']);
  checkVideo = (ob: any, size_mb = 100): boolean => this.validateFile(ob, this.checkVideoExt, size_mb, 'Video', ['mp4', 'webp', 'mkv', 'mov']);
  checkImagePdf = (ob: any, size_mb = 2): boolean => {
    const validFunc = (filename: string) => this.checkImageExt(filename) || this.checkPdfExt(filename);
    return this.validateFile(ob, validFunc, size_mb, 'File', ['jpg', 'png', 'pdf']);
  };

  private validateFile = (ob: any, checkFunc: (filename: string) => boolean, size_mb: number, typeName: string, allowed: string[]): boolean => {
    const obj = $(ob);
    let err = false;

    if (!checkFunc(obj.val())) {
      err = true;
      Modal.error({ title: `${typeName} Error!`, content: `Invalid ${typeName.toLowerCase()}! Only ${allowed.join(', ')} files are allowed.` });
    }

    if (!err) {
      const file = obj[0];
      const size = file.files[0].size / 1024;
      if (size > size_mb * 1024) {
        err = true;
        Modal.error({ title: `${typeName} Size Error!`, content: `Can't upload! This file is larger than ${size_mb} MB.` });
      }
    }

    if (err) obj.val('');
    return !err;
  };

  /** UI helpers */
  showLoader = () => {
    const el = '<div class="bodycover"></div><div class="loader"><i class="fa fa-spin fa-circle-notch"></i></div>';
    $(".bodycover,.loader").remove();
    $("body").prepend(el);
  };

  hideLoader = () => $(".bodycover,.loader").remove();

  showAlertMsg = (msg: string, type: 'S' | 'E' = 'S', t = 4000) => {
    const el = `<div class="alertmsg">
      <div class="alert alert-${type === 'S' ? 'success' : 'danger'} shadow" role="alert">
        <i class="fa fa-${type === 'S' ? 'check' : 'times'}-circle"></i> ${msg}
      </div>
    </div>`;
    $(".alertmsg").remove();
    $("body").prepend(el);
    $(".alertmsg").delay(t).fadeOut('slow');
  };

  hideAlertMsg = () => $(".alertmsg").remove();

  /** Modal helpers */
  showModal = (obj: any, fullHeight = false, fixFullHeight = false, decreaseHeight?: number) => {
    let n = 150;
    if (obj.find(".modal-dialog").hasClass("modal-fullx")) n = 116;
    if (obj.find(".modal-dialog").hasClass("modal-full")) n = 130;
    if (decreaseHeight) n = decreaseHeight;

    if (fullHeight) {
      const h = $(window).outerHeight() - n;
      obj.find(".modal-body").css({ 'overflow': 'auto', height: fixFullHeight ? `${h}px` : 'auto', 'max-height': !fixFullHeight ? `${h}px` : undefined });
    }

    obj.modal();
    obj.find(".modal-body").scrollTop(0);
    if (obj.hasClass('no-backdrop')) $('.modal-backdrop').remove();
  };

  hideModal = (obj: any) => {
    obj.modal('hide');
    $('body').removeClass('modal-open');
    $('.modal-backdrop').remove();
  };

  redirect = (url: string) => { window.location.href = url; };

  nv = (v: any): number => {
    v = $.trim(v);
    return isNaN(v) ? 0 : v * 1;
  };

  getDistance = (lat1: number, lon1: number, lat2: number, lon2: number): number => {
    const R = 6371; // km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const lt1 = lat1 * Math.PI / 180;
    const lt2 = lat2 * Math.PI / 180;
    const a = Math.sin(dLat/2)**2 + Math.sin(dLon/2)**2 * Math.cos(lt1) * Math.cos(lt2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  };

  inWords = (num: number): string => {
    if (!this.nv(num)) return '';
    const a = ['', 'one ', 'two ', 'three ', 'four ', 'five ', 'six ', 'seven ', 'eight ', 'nine ', 'ten ',
      'eleven ', 'twelve ', 'thirteen ', 'fourteen ', 'fifteen ', 'sixteen ', 'seventeen ', 'eighteen ', 'nineteen '];
    const b = ['', '', 'twenty', 'thirty', 'forty', 'fifty', 'sixty', 'seventy', 'eighty', 'ninety'];
    if ((num = Number(num.toString())) > 999999999) return 'overflow';
    const n = ('000000000' + num).slice(-9).match(/^(\d{2})(\d{2})(\d{2})(\d{1})(\d{2})$/);
    if (!n) return '';
    let str = '';
    str += n[1] !== '00' ? (a[Number(n[1])] || b[Number(n[1][0])] + ' ' + a[Number(n[1][1])]) + 'crore ' : '';
    str += n[2] !== '00' ? (a[Number(n[2])] || b[Number(n[2][0])] + ' ' + a[Number(n[2][1])]) + 'lakh ' : '';
    str += n[3] !== '00' ? (a[Number(n[3])] || b[Number(n[3][0])] + ' ' + a[Number(n[3][1])]) + 'thousand ' : '';
    str += n[4] !== '0' ? (a[Number(n[4])] || b[Number(n[4][0])] + ' ' + a[Number(n[4][1])]) + 'hundred ' : '';
    str += n[5] !== '00' ? 'and ' + (a[Number(n[5])] || b[Number(n[5][0])] + ' ' + a[Number(n[5][1])]) : '';
    return str.trim();
  };

  getDate = (d?: Date | string, df?: string): string => {
    if (!d) d = new Date();
    return df ? moment(d).format(df) : moment(d).format('DD MMM YYYY');
  };
  geLoggedinName = (): string => { 
    return("Simphiwe Magagula")
  }; 

  toBase64 = (file: File): Promise<string | ArrayBuffer | null> =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = (error) => reject(error);
    });

  resizeImage = (imgOb: HTMLImageElement, w: number): HTMLCanvasElement => {
    const sw = imgOb.naturalWidth || imgOb.width;
    const sh = imgOb.naturalHeight || imgOb.height;
    if (sw <= w) w = sw;
    const h = Math.round((w / sw) * sh);
    const canvas = document.createElement('canvas');
    canvas.width = w;
    canvas.height = h;
    canvas.getContext('2d')?.drawImage(imgOb, 0, 0, w, h);
    return canvas;
  };

  imageDataToFile = (imageData: string): File => {
    const byteCharacters = atob(imageData.split(';base64,')[1]);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) byteNumbers[i] = byteCharacters.charCodeAt(i);
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: 'image/jpeg' });
    const filename = `${Date.now()}${Math.floor(Math.random() * 100)}.jpg`;
    return new File([blob], filename, { type: 'image/jpeg' });
  };

roundTo = (num: number, places: number): number => {
  const factor = 10 ** places;
  return Math.round(num * factor) / factor;
};
  Orientation: Record<string, OrientationType> = Object.freeze({
    PORTRAIT: 'portrait',
    LANSCAPE: 'landscape',
  });
}

export default new Util();
