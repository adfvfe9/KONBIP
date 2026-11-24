import { IconSearch } from "./Icons";

// readOnly=true 이면 눌렀을 때 onClick 만 실행하는 버튼 모양 (홈 화면용)
export default function SearchBar({ value = "", onChange, onSubmit, onClick, readOnly = false, autoFocus = false }) {
  if (readOnly) {
    return (
      <button type="button" className="searchbar searchbar--button" onClick={onClick}>
        <IconSearch />
        <span>꿀조합을 검색 해보세요!</span>
      </button>
    );
  }
  return (
    <form className="searchbar" onSubmit={(e) => { e.preventDefault(); onSubmit?.(value); }}>
      <IconSearch />
      <input
        value={value}
        onChange={(e) => onChange?.(e.target.value)}
        placeholder="꿀조합을 검색 해보세요!"
        autoFocus={autoFocus}
      />
    </form>
  );
}
