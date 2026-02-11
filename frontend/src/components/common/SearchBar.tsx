import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export type SearchType = 'all' | 'birds' | 'users' | 'friends' | 'groups' | 'my-groups' | 'posts';

type SearchBarProps = {
  searchType?: SearchType;
  placeholder?: string;
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onSearch?: (query: string, type: SearchType) => void;
  autoSubmit?: boolean;
};

function SearchBar({ 
  searchType = 'all', 
  placeholder = 'Search',
  onChange, 
  onSearch,
  autoSubmit = true
}: SearchBarProps) {
  const [query, setQuery] = useState('');
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
    if (onChange) {
      onChange(e);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (query.trim()) {
      if (onSearch) {
        onSearch(query.trim(), searchType);
      } else if (searchType === 'all') {
        navigate(`/search?q=${encodeURIComponent(query.trim())}`);
      } else {
        navigate(`/search?q=${encodeURIComponent(query.trim())}&type=${searchType}`);
      }
    }
  };

  return (
    <div className='flex items-center justify-center w-full'>
      <form onSubmit={autoSubmit ? handleSubmit : undefined} className="flex-1">
        <div className="flex flex-row items-center min-w-0 px-4 py-2 border border-gray-300 rounded">
          <img
            src="/src/assets/search.svg"
            alt="search"
            className="w-4 h-4 shrink mr-3 opacity-80"
          />
          <input
            type="text"
            placeholder={placeholder}
            value={query}
            onChange={handleChange}
            className="flex-1 min-w-0 text-sm outline-transparent bg-transparent w-full"
          />
        </div>
      </form>
    </div>
  );
}

export default SearchBar;
