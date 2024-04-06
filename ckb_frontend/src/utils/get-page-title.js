import defaultSettings from "@/settings";
import store from "@/store";

const title = store.state.config.systemTitle;

export default function getPageTitle(pageTitle) {
  if (pageTitle) {
    return `${pageTitle} - ${title}`;
  }
  return `${title}`;
}
